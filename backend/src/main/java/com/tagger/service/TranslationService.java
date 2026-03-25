package com.tagger.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import com.tagger.model.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Core AI inference engine for natural language to Danbooru tag translation.
 * <p>
 * This service leverages the ONNX Runtime and Deep Java Library (DJL) HuggingFace tokenizers
 * to execute local, offline Large Language Model (LLM) inference (e.g., Phi-3-mini).
 * It handles the full lifecycle of the generation process including prompt templating,
 * tokenization, autoregressive decoding, and raw text cleanup.
 *
 * <b>Critical Engineering Notes:</b>
 * <ul>
 * <li><b>Native Memory Management:</b> ONNX Runtime requires manual memory management for C++ tensor pointers.
 * The {@code runInference} loop specifically decouples the lifecycle of transient tensors and the KV cache
 * to prevent fatal use-after-close segfaults and severe native memory leaks.</li>
 * <li><b>Lazy Initialization:</b> The heavy ONNX {@code OrtSession} and Tokenizer are loaded into memory
 * only on the first translation request to optimize application startup time.</li>
 * <li><b>Validation:</b> All generated outputs are cross-referenced with the {@link TagDatabaseService}
 * to ensure only valid tags are returned to the client.</li>
 * </ul>
 */
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final TagDatabaseService tagDatabaseService;

    private static volatile OrtEnvironment ortEnv;
    private static volatile OrtSession ortSession;
    private static volatile HuggingFaceTokenizer tokenizer;

    public TranslationService(TagDatabaseService tagDatabaseService) {
        this.tagDatabaseService = tagDatabaseService;
    }

    public TranslationResponse translate(String naturalText, String modelFamily) {
        if (!Files.exists(LlmModelService.MODEL_PATH) || !Files.exists(LlmModelService.TOKENIZER_PATH)) {
            log.warn("ONNX model or tokenizer not found. Translation cannot proceed.");
            return new TranslationResponse(Collections.emptyList(), modelFamily, "Model file not found. Please download the model.");
        }

        try {
            initializeResources();
            String prompt = createPrompt(naturalText);
            String outputText = runInference(prompt);
            List<String> validTags = parseAndValidateTags(outputText);

            String warning = validTags.isEmpty() ? "The model returned no valid tags." : null;
            return new TranslationResponse(validTags, modelFamily, warning);

        } catch (OrtException e) {
            log.error("ONNX Runtime exception during translation", e);
            return new TranslationResponse(Collections.emptyList(), modelFamily, "Error during model inference.");
        } catch (IOException e) {
            log.error("Failed to load tokenizer resources", e);
            return new TranslationResponse(Collections.emptyList(), modelFamily, "Error loading tokenizer.");
        } catch (Exception e) {
            log.error("Unexpected error during translation", e);
            return new TranslationResponse(Collections.emptyList(), modelFamily, "An unexpected error occurred.");
        }
    }

    private synchronized void initializeResources() throws OrtException, IOException {
        if (ortEnv == null) {
            ortEnv = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.addConfigEntry("session.dynamic_block_base", "2");
            ortSession = ortEnv.createSession(LlmModelService.MODEL_PATH.toString(), options);
            tokenizer = HuggingFaceTokenizer.newInstance(LlmModelService.TOKENIZER_PATH);
            log.info("ONNX Runtime and tokenizer initialized successfully from {}.", LlmModelService.MODEL_DIR);
        }
    }

    private String createPrompt(String userInput) {
        return "<|system|>Extract visual characteristics, actions, and objects from the following text and convert them into comma-separated Danbooru tags. Output ONLY the tags.<|end|><|user|>" + userInput + "<|end|><|assistant|>";
    }

    private String runInference(String prompt) throws OrtException {
        Encoding encoding = tokenizer.encode(prompt);
        long[] inputIds = encoding.getIds();
        List<Long> generatedTokens = new ArrayList<>();

        int numLayers = 32;
        Map<String, OnnxTensor> currentKvCache = new HashMap<>();

        for (int i = 0; i < numLayers; ++i) {
            currentKvCache.put("past_key_values." + i + ".key", createEmptyPast(ortEnv));
            currentKvCache.put("past_key_values." + i + ".value", createEmptyPast(ortEnv));
        }

        long[] currentInputIds = inputIds;
        long[] attentionMask = LongStream.generate(() -> 1L).limit(currentInputIds.length).toArray();

        OrtSession.Result previousResult = null;

        try {
            for (int i = 0; i < 150; i++) {
                Map<String, OnnxTensor> inputs = new HashMap<>();

                OnnxTensor inputIdsTensor = OnnxTensor.createTensor(ortEnv, new long[][]{currentInputIds});
                OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(ortEnv, new long[][]{attentionMask});

                inputs.put("input_ids", inputIdsTensor);
                inputs.put("attention_mask", attentionMaskTensor);
                inputs.putAll(currentKvCache);

                OrtSession.Result currentResult = ortSession.run(inputs);

                inputIdsTensor.close();
                attentionMaskTensor.close();

                float[][][] logits = (float[][][]) currentResult.get(0).getValue();
                long nextTokenId = getNextTokenId(logits[0]);

                if (isEosToken(nextTokenId)) {
                    currentResult.close();
                    break;
                }

                generatedTokens.add(nextTokenId);
                currentInputIds = new long[]{nextTokenId};

                attentionMask = Arrays.copyOf(attentionMask, attentionMask.length + 1);
                attentionMask[attentionMask.length - 1] = 1L;

                Map<String, OnnxTensor> nextKvCache = new HashMap<>();
                for (int j = 0; j < numLayers; ++j) {
                    nextKvCache.put("past_key_values." + j + ".key", (OnnxTensor) currentResult.get(j * 2 + 1));
                    nextKvCache.put("past_key_values." + j + ".value", (OnnxTensor) currentResult.get(j * 2 + 2));
                }

                if (previousResult != null) {
                    previousResult.close();
                } else {
                    for (OnnxTensor tensor : currentKvCache.values()) {
                        tensor.close();
                    }
                }

                currentKvCache = nextKvCache;
                previousResult = currentResult;
            }
        } finally {
            if (previousResult != null) {
                previousResult.close();
            } else {
                for (OnnxTensor tensor : currentKvCache.values()) {
                    tensor.close();
                }
            }
        }

        long[] finalTokens = generatedTokens.stream().mapToLong(l -> l).toArray();
        return tokenizer.decode(finalTokens);
    }

    private OnnxTensor createEmptyPast(OrtEnvironment env) throws OrtException {
        long batchSize = 1;
        long numKvHeads = 32;
        long sequenceLength = 0;
        long headDim = 96;

        long[] shape = new long[]{batchSize, numKvHeads, sequenceLength, headDim};

        FloatBuffer emptyBuffer = FloatBuffer.allocate(0);

        return OnnxTensor.createTensor(env, emptyBuffer, shape);
    }

    private long getNextTokenId(float[][] logits) {
        int nextTokenLogitIndex = logits.length - 1;
        float maxLogit = -Float.MAX_VALUE;
        long nextTokenId = -1;

        for (int j = 0; j < logits[nextTokenLogitIndex].length; j++) {
            if (logits[nextTokenLogitIndex][j] > maxLogit) {
                maxLogit = logits[nextTokenLogitIndex][j];
                nextTokenId = j;
            }
        }
        return nextTokenId;
    }

    private boolean isEosToken(long tokenId) {
        return tokenId == 32007;
    }

    private List<String> parseAndValidateTags(String modelOutput) {
        return Arrays.stream(modelOutput.split(","))
                .map(tag -> tag.trim().toLowerCase().replace(' ', '_'))
                .filter(tag -> !tag.isEmpty())
                .filter(tag -> tagDatabaseService.validate(tag).valid())
                .collect(Collectors.toList());
    }
}
package com.tagger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code LlmModelService} manages the lifecycle and state of the ONNX Large Language Model (LLM)
 * and its associated tokenizer. This service is responsible for downloading the necessary model
 * and tokenizer files from remote repositories, tracking the download progress, and verifying
 * the readiness of the model for use.
 *
 * <p>It utilizes modern Java features such as {@link HttpClient} for efficient, non-blocking
 * network operations and virtual threads for concurrent task execution, ensuring that model
 * downloads do not block the main application thread. The service also handles partial downloads
 * and provides cleanup mechanisms in case of download failures.
 *
 * <p>The model and tokenizer files are stored locally in a predefined directory structure.
 *
 * <p>Usage:
 * <pre>{@code
 * LlmModelService modelService = new LlmModelService();
 * if (!modelService.isModelReady()) {
 *     modelService.startDownload();
 *     // Monitor progress using modelService.getDownloadProgress()
 * }
 * }</pre>
 *
 * @see HttpClient
 * @see Executors#newVirtualThreadPerTaskExecutor()
 * @see Path
 */
public class LlmModelService {
    private static final Logger log = LoggerFactory.getLogger(LlmModelService.class);

    private static final String MODEL_FILENAME = "phi3-mini-4k-instruct-cpu-int4-rtn-block-32-acc-level-4.onnx";
    private static final String MODEL_URL = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-onnx/resolve/main/cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4/" + MODEL_FILENAME;
    private static final String MODEL_DATA_URL = MODEL_URL + ".data";
    private static final String TOKENIZER_URL = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-onnx/resolve/main/cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4/tokenizer.json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";

    public static final Path MODEL_DIR = Paths.get("data", "models");
    public static final Path MODEL_PATH = MODEL_DIR.resolve(MODEL_FILENAME);
    private static final Path MODEL_DATA_PATH = MODEL_DIR.resolve(MODEL_FILENAME + ".data");
    public static final Path TOKENIZER_PATH = MODEL_DIR.resolve("tokenizer.json");

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);
    private final AtomicInteger downloadProgress = new AtomicInteger(0);
    private final HttpClient httpClient;

    public LlmModelService() {
        this.httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public boolean isModelReady() {
        return Files.exists(MODEL_PATH) && Files.exists(MODEL_DATA_PATH) && Files.exists(TOKENIZER_PATH);
    }

    public boolean isDownloading() {
        return isDownloading.get();
    }

    public int getDownloadProgress() {
        return downloadProgress.get();
    }

    public String getModelDirectory() {
        return MODEL_DIR.toAbsolutePath().toString();
    }

    public void startDownload() {
        if (isDownloading.compareAndSet(false, true)) {
            Executors.newSingleThreadExecutor().submit(this::performDownload);
        }
    }

    private void performDownload() {
        log.info("Starting model download...");
        try {
            Files.createDirectories(MODEL_DIR);

            downloadFile(TOKENIZER_URL, TOKENIZER_PATH, 0, 5);
            downloadFile(MODEL_URL, MODEL_PATH, 5, 50);
            downloadFile(MODEL_DATA_URL, MODEL_DATA_PATH, 50, 100);

            log.info("Model and tokenizer download complete.");
        } catch (IOException | InterruptedException e) {
            log.error("Download failed", e);
            cleanupPartialFiles();
        } finally {
            isDownloading.set(false);
        }
    }

    private void downloadFile(String url, Path finalPath, int startProgress, int endProgress) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .build();
        Path tempPath = finalPath.resolveSibling(finalPath.getFileName() + ".tmp");

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to download file from " + url + ". Status code: " + response.statusCode());
            }

            long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            long bytesRead = 0;

            try (InputStream is = response.body(); FileOutputStream fos = new FileOutputStream(tempPath.toFile())) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, n);
                    bytesRead += n;
                    if (totalBytes > 0) {
                        int currentFileProgress = (int) ((bytesRead * 100L) / totalBytes);
                        int overallProgress = startProgress + (currentFileProgress * (endProgress - startProgress) / 100);
                        downloadProgress.set(overallProgress);
                    }
                }
            }

            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            downloadProgress.set(endProgress);

        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    private void cleanupPartialFiles() {
        try {
            Files.deleteIfExists(MODEL_PATH);
            Files.deleteIfExists(MODEL_DATA_PATH);
            Files.deleteIfExists(TOKENIZER_PATH);
            Files.deleteIfExists(MODEL_PATH.resolveSibling(MODEL_PATH.getFileName() + ".tmp"));
            Files.deleteIfExists(MODEL_DATA_PATH.resolveSibling(MODEL_DATA_PATH.getFileName() + ".tmp"));
            Files.deleteIfExists(TOKENIZER_PATH.resolveSibling(TOKENIZER_PATH.getFileName() + ".tmp"));
        } catch (IOException cleanupEx) {
            log.error("Failed to cleanup partial download files.", cleanupEx);
        }
    }
}

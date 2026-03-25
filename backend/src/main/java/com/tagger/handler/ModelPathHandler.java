package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.server.JsonUtil;
import com.tagger.service.LlmModelService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * {@code ModelPathHandler} is an HTTP handler that provides the absolute path
 * to the directory where the LLM models are stored.
 *
 * <p>This handler responds to GET requests at {@code /api/model/path}. Upon receiving
 * a request, it retrieves the model directory path from the {@link LlmModelService}
 * and returns it in a JSON object.
 *
 * <p>Example JSON response:
 * <pre>{@code
 * {
 *   "path": "C:\\Users\\user\\Latent-Tagger\\data\\models"
 * }
 * }</pre>
 *
 * @see LlmModelService#getModelDirectory()
 */
public class ModelPathHandler implements HttpHandler {
    private final LlmModelService llmModelService;

    public ModelPathHandler(LlmModelService llmModelService) {
        this.llmModelService = llmModelService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String modelPath = llmModelService.getModelDirectory();
            String jsonResponse = JsonUtil.toJson(Map.of("path", modelPath));

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}

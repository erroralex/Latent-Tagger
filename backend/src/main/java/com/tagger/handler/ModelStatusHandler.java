package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.server.JsonUtil;
import com.tagger.service.LlmModelService;

import java.io.IOException;
import java.util.Map;

/**
 * {@code ModelStatusHandler} is an {@link HttpHandler} that provides the current status
 * of the Large Language Model (LLM) to clients. It handles GET requests to an endpoint
 * (e.g., {@code /api/model/status}) and returns a JSON response indicating:
 * <ul>
 *     <li>{@code ready}: A boolean indicating if the model files are fully downloaded and ready for use.</li>
 *     <li>{@code downloading}: A boolean indicating if a model download is currently in progress.</li>
 *     <li>{@code progress}: An integer representing the current download progress (0-100) if downloading.</li>
 * </ul>
 *
 * <p>This handler interacts with the {@link LlmModelService} to retrieve the model's state.
 *
 * @see LlmModelService
 * @see JsonUtil
 * @see HttpHandler
 */
public class ModelStatusHandler implements HttpHandler {
    private final LlmModelService modelService;

    public ModelStatusHandler(LlmModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Map<String, Object> status = Map.of(
                "ready", modelService.isModelReady(),
                "downloading", modelService.isDownloading(),
                "progress", modelService.getDownloadProgress()
        );
        JsonUtil.sendJson(exchange, 200, status);
    }
}

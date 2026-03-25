package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.server.JsonUtil;
import com.tagger.service.LlmModelService;

import java.io.IOException;
import java.util.Map;

/**
 * {@code ModelDownloadHandler} is an {@link HttpHandler} that provides an API endpoint
 * to initiate the download of the Large Language Model (LLM) files. It specifically
 * handles POST requests to an endpoint (e.g., {@code /api/model/download}).
 *
 * <p>Upon receiving a POST request, this handler delegates the task of starting the
 * model download to the {@link LlmModelService}. It responds with a 202 Accepted status
 * to indicate that the download process has been initiated.
 *
 * <p>This handler does not require authentication, as model download is a public operation.
 *
 * @see LlmModelService
 * @see JsonUtil
 * @see HttpHandler
 */
public class ModelDownloadHandler implements HttpHandler {
    private final LlmModelService modelService;

    public ModelDownloadHandler(LlmModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        modelService.startDownload();
        JsonUtil.sendJson(exchange, 202, Map.of("status", "Download started"));
    }
}

package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.model.TranslationRequest;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;
import com.tagger.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TranslateHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(TranslateHandler.class);
    private final TranslationService translationService;

    public TranslateHandler(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        String requestBody = "";
        try (InputStream is = exchange.getRequestBody()) {
            requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Received /api/translate request with body: {}", requestBody);

            TranslationRequest request = JsonUtil.fromJson(requestBody, TranslationRequest.class);

            if (request == null || request.naturalText() == null) {
                log.warn("Translation request has invalid body: {}", requestBody);
                JsonUtil.sendJson(exchange, 400, Map.of("error", "invalid request body"));
                return;
            }

            var response = translationService.translate(request.naturalText(), request.modelFamily());
            log.info("Translation successful for input: '{}'", request.naturalText());
            JsonUtil.sendJson(exchange, 200, response);

        } catch (Exception e) {
            log.error("Error processing translation request. Body: {}", requestBody, e);
            JsonUtil.sendJson(exchange, 500, Map.of("error", "Internal server error during translation."));
        }
    }
}

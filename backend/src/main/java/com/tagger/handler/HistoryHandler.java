package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;
import com.tagger.service.HistoryService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryHandler implements HttpHandler {

    private final HistoryService historyService;

    public HistoryHandler(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            handleGet(exchange);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        int limit = 50;
        if (params.containsKey("limit")) {
            limit = Integer.parseInt(params.get("limit"));
        }

        var entries = historyService.getRecent(limit);
        JsonUtil.sendJson(exchange, 200, Map.of("entries", entries));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), "UTF-8");
            HistoryRequest request = JsonUtil.fromJson(body, HistoryRequest.class);

            if (request == null || request.tags == null) {
                JsonUtil.sendJson(exchange, 400, Map.of("error", "invalid request body"));
                return;
            }

            historyService.save(request.tags, request.nlInput, request.model);
            JsonUtil.sendJson(exchange, 200, Map.of("ok", true));

        } catch (Exception e) {
            JsonUtil.sendJson(exchange, 400, Map.of("error", "invalid request body"));
        }
    }

    private Map<String, String> queryToMap(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), "");
            }
        }
        return result;
    }

    private static class HistoryRequest {
        public List<String> tags;
        public String nlInput;
        public String model;
    }
}

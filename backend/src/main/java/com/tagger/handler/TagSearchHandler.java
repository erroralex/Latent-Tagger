package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;
import com.tagger.service.TagDatabaseService;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code TagSearchHandler} is an {@link HttpHandler} that provides an API endpoint
 * for searching tags within the application's tag database. It specifically handles
 * GET requests to {@code /api/tags/search}.
 *
 * <p>Clients can query for tags by providing a 'q' parameter in the URL.
 * An optional 'limit' parameter can be used to control the maximum number of
 * search results returned (defaulting to 20, with a hard cap at 50).
 *
 * <p>This handler relies on {@link TagDatabaseService} to perform the actual tag search
 * and uses {@link AuthFilter} to ensure that only authenticated requests are processed.
 * Responses are formatted as JSON using {@link JsonUtil}.
 *
 * @see TagDatabaseService
 * @see AuthFilter
 * @see JsonUtil
 * @see HttpHandler
 */
public class TagSearchHandler implements HttpHandler {

    private final TagDatabaseService tagDatabaseService;

    public TagSearchHandler(TagDatabaseService tagDatabaseService) {
        this.tagDatabaseService = tagDatabaseService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String query = params.get("q");

        if (query == null || query.isBlank()) {
            JsonUtil.sendJson(exchange, 400, Map.of("error", "q parameter required"));
            return;
        }

        int limit = 20;
        if (params.containsKey("limit")) {
            limit = Math.min(50, Integer.parseInt(params.get("limit")));
        }

        var results = tagDatabaseService.search(query, limit);
        JsonUtil.sendJson(exchange, 200, Map.of("results", results));
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
}

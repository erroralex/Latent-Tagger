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

public class ImplicationsHandler implements HttpHandler {

    private final TagDatabaseService tagDatabaseService;

    public ImplicationsHandler(TagDatabaseService tagDatabaseService) {
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
        String tag = params.get("tag");

        if (tag == null || tag.isBlank()) {
            JsonUtil.sendJson(exchange, 400, Map.of("error", "tag parameter required"));
            return;
        }

        var implications = tagDatabaseService.getImplications(tag);
        JsonUtil.sendJson(exchange, 200, Map.of("implications", implications));
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

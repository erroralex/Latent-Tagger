package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class PresetsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        try (InputStream is = PresetsHandler.class.getResourceAsStream("/data/presets.json")) {
            if (is == null) {
                JsonUtil.sendJson(exchange, 404, Map.of("error", "presets.json not found"));
                return;
            }
            Object presets = JsonUtil.fromJson(new String(is.readAllBytes()), Object.class);
            JsonUtil.sendJson(exchange, 200, presets);
        } catch (Exception e) {
            JsonUtil.sendJson(exchange, 500, Map.of("error", "Failed to read presets.json"));
        }
    }
}

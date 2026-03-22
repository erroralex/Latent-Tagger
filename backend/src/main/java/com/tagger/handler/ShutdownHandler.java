package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ShutdownHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Received request for /api/shutdown from {}", exchange.getRemoteAddress());

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        log.info("Shutdown request authorized. Shutting down server.");
        JsonUtil.sendJson(exchange, 200, Map.of("status", "shutting down"));

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        });
    }
}

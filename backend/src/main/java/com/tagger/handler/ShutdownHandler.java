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

/**
 * {@code ShutdownHandler} is an {@link HttpHandler} that provides an endpoint
 * to gracefully shut down the application. It listens for POST requests to
 * {@code /api/shutdown}.
 *
 * <p>Upon receiving an authorized shutdown request, it sends an immediate
 * success response to the client and then initiates a delayed system exit.
 * The delay is introduced to allow the HTTP response to be fully sent before
 * the application terminates.
 *
 * <p>Authentication is enforced using {@link AuthFilter} to prevent unauthorized
 * shutdowns.
 *
 * @see AuthFilter
 * @see JsonUtil
 * @see HttpHandler
 */
public class ShutdownHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Received request for /api/shutdown from {}", exchange.getRemoteAddress());

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
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

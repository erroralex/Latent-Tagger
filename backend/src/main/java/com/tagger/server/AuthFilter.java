package com.tagger.server;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    public static boolean isAuthorized(HttpExchange exchange, String expectedToken) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String expectedHeader = "Bearer " + expectedToken;
        boolean authorized = expectedHeader.equals(authHeader);
        if (!authorized) {
            log.warn("Authorization failed for {}. Provided token: {}", exchange.getRemoteAddress(), authHeader);
        }
        return authorized;
    }

    public static boolean rejectIfUnauthorized(HttpExchange exchange, String token) throws IOException {
        if (!isAuthorized(exchange, token)) {
            String response = "{\"error\":\"unauthorized\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(401, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
            return true;
        }
        return false;
    }
}

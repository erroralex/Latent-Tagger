package com.tagger.server;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * {@code AuthFilter} provides utility methods for handling API key-based authentication
 * for HTTP requests. It is designed to be used with {@link HttpExchange} objects
 * from the {@code com.sun.net.httpserver} package.
 *
 * <p>This class facilitates checking the "Authorization" header of incoming requests
 * against a predefined bearer token. If the token does not match, it can reject
 * the request with an unauthorized (401) HTTP status code.
 *
 * <p>Usage:
 * <pre>{@code
 * public class MyHandler implements HttpHandler {
 *     private static final String API_KEY = "your_secret_api_key";
 *
 *     @Override
 *     public void handle(HttpExchange exchange) throws IOException {
 *         if (AuthFilter.rejectIfUnauthorized(exchange, API_KEY)) {
 *             return; // Request was unauthorized and a response was sent
 *         }
 *         // Proceed with authorized request handling
 *     }
 * }
 * }</pre>
 *
 * @see HttpExchange
 */
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

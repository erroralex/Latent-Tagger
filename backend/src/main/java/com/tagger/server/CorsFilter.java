package com.tagger.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * {@code CorsFilter} provides utility methods for handling Cross-Origin Resource Sharing (CORS)
 * for HTTP requests within the {@code com.sun.net.httpserver} framework.
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>Adding necessary CORS headers to HTTP responses to allow cross-origin requests.</li>
 *     <li>Handling HTTP OPTIONS preflight requests by sending appropriate headers and
 *         a 204 No Content status.</li>
 * </ul>
 *
 * <p>In a production environment, it is highly recommended to restrict the {@code Access-Control-Allow-Origin}
 * header to specific, trusted origins instead of using a wildcard ("*") for security reasons.
 *
 * <p>Usage:
 * <pre>{@code
 * public class MyHandler implements HttpHandler {
 *     @Override
 *     public void handle(HttpExchange exchange) throws IOException {
 *         CorsFilter.addCorsHeaders(exchange);
 *         if (CorsFilter.handleOptionsRequest(exchange)) {
 *             return; // Preflight request handled
 *         }
 *         // Proceed with actual request handling
 *     }
 * }
 * }</pre>
 *
 * @see HttpExchange
 */
public class CorsFilter {

    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public static boolean handleOptionsRequest(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }
}

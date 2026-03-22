package com.tagger.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * Handles CORS (Cross-Origin Resource Sharing) preflight requests and adds required headers.
 */
public class CorsFilter {

    /**
     * Adds CORS headers to the response. In a production environment, the origin should be more restrictive.
     *
     * @param exchange The HTTP exchange.
     */
    public static void addCorsHeaders(HttpExchange exchange) {
        // Use set instead of add to prevent duplicate headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Handles the OPTIONS preflight request. The headers are already added by the wrapper.
     *
     * @param exchange The HTTP exchange.
     * @return true if the request was an OPTIONS request and was handled, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static boolean handleOptionsRequest(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content
            return true;
        }
        return false;
    }
}

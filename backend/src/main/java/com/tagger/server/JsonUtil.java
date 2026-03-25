package com.tagger.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility class for JSON serialization and deserialization using Jackson.
 * This class provides static methods to convert Java objects to JSON strings,
 * JSON strings to Java objects, and to send JSON responses over HTTP.
 *
 * <p>It configures the {@link ObjectMapper} to ignore unknown properties during
 * deserialization, providing flexibility when dealing with evolving API contracts.
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String toJson(Object obj) throws JsonProcessingException {
        return MAPPER.writeValueAsString(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }

    public static void sendJson(HttpExchange ex, int status, Object body) throws IOException {
        byte[] jsonBytes = MAPPER.writeValueAsBytes(body);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, jsonBytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(jsonBytes);
        }
    }
}

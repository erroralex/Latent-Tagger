package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * {@code PresetsHandler} is an {@link HttpHandler} that serves predefined tag presets
 * from a JSON file. It specifically handles GET requests to retrieve the contents
 * of {@code /data/presets.json} from the application's resources.
 *
 * <p>This handler ensures that only authenticated requests can access the presets
 * by utilizing the {@link AuthFilter}. If the {@code presets.json} file is not found,
 * it returns a 404 Not Found error. Any other issues during file reading or JSON
 * parsing result in a 500 Internal Server Error.
 *
 * <p>The content of {@code presets.json} is expected to be a valid JSON structure,
 * which is then directly sent as the HTTP response body.
 *
 * @see AuthFilter
 * @see JsonUtil
 * @see HttpHandler
 */
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

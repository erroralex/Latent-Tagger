package com.tagger.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tagger.LatentTaggerApplication;
import com.tagger.model.TagSearchResult;
import com.tagger.server.AuthFilter;
import com.tagger.server.JsonUtil;
import com.tagger.service.TagDatabaseService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code TagValidateHandler} is an {@link HttpHandler} that provides an API endpoint
 * for validating a list of tags against the application's tag database. It specifically
 * handles POST requests to an endpoint (e.g., {@code /api/tags/validate}).
 *
 * <p>The handler expects a JSON request body containing a list of tag strings.
 * For each tag in the list, it uses the {@link TagDatabaseService} to validate
 * its existence and resolve any aliases.
 *
 * <p>Authentication is enforced using {@link AuthFilter}. The response is a JSON
 * object containing a list of {@link TagSearchResult} objects, one for each
 * validated tag.
 *
 * @see TagDatabaseService
 * @see TagSearchResult
 * @see AuthFilter
 * @see JsonUtil
 * @see HttpHandler
 */
public class TagValidateHandler implements HttpHandler {

    private final TagDatabaseService tagDatabaseService;

    public TagValidateHandler(TagDatabaseService tagDatabaseService) {
        this.tagDatabaseService = tagDatabaseService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        if (AuthFilter.rejectIfUnauthorized(exchange, LatentTaggerApplication.AUTH_TOKEN)) {
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), "UTF-8");
            ValidateRequest request = JsonUtil.fromJson(body, ValidateRequest.class);

            if (request == null || request.tags == null) {
                JsonUtil.sendJson(exchange, 400, Map.of("error", "invalid request body"));
                return;
            }

            List<TagSearchResult> results = request.tags.stream()
                    .map(tagDatabaseService::validate)
                    .collect(Collectors.toList());

            JsonUtil.sendJson(exchange, 200, Map.of("results", results));

        } catch (Exception e) {
            JsonUtil.sendJson(exchange, 400, Map.of("error", "invalid request body"));
        }
    }

    private static class ValidateRequest {
        public List<String> tags;
    }
}

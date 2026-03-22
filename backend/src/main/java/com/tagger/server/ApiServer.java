package com.tagger.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tagger.handler.HistoryHandler;
import com.tagger.handler.ImplicationsHandler;
import com.tagger.handler.PresetsHandler;
import com.tagger.handler.ShutdownHandler;
import com.tagger.handler.TagSearchHandler;
import com.tagger.handler.TagValidateHandler;
import com.tagger.handler.TranslateHandler;
import com.tagger.service.HistoryService;
import com.tagger.service.TagDatabaseService;
import com.tagger.service.TranslationService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer {

    private final HttpServer server;

    public ApiServer(TagDatabaseService tagDatabaseService, TranslationService translationService, HistoryService historyService) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        createContexts(tagDatabaseService, translationService, historyService);
    }

    private void createContexts(TagDatabaseService tagDb, TranslationService transSvc, HistoryService histSvc) {
        server.createContext("/api/tags/search", withCors(new TagSearchHandler(tagDb)));
        server.createContext("/api/tags/validate", withCors(new TagValidateHandler(tagDb)));
        server.createContext("/api/tags/implications", withCors(new ImplicationsHandler(tagDb)));
        server.createContext("/api/translate", withCors(new TranslateHandler(transSvc)));
        server.createContext("/api/presets", withCors(new PresetsHandler()));
        server.createContext("/api/history", withCors(new HistoryHandler(histSvc)));
        server.createContext("/api/shutdown", withCors(new ShutdownHandler()));
    }

    /**
     * Wraps an HttpHandler with CORS handling logic.
     *
     * @param handler The original handler.
     * @return A new handler that first applies CORS logic and then delegates to the original handler.
     */
    private static HttpHandler withCors(HttpHandler handler) {
        return exchange -> {
            CorsFilter.addCorsHeaders(exchange);
            if (CorsFilter.handleOptionsRequest(exchange)) {
                return;
            }
            handler.handle(exchange);
        };
    }

    public void start() {
        server.start();
    }

    public InetSocketAddress getAddress() {
        return server.getAddress();
    }
}

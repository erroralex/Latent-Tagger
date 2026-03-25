package com.tagger.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tagger.handler.*;
import com.tagger.service.HistoryService;
import com.tagger.service.LlmModelService;
import com.tagger.service.TagDatabaseService;
import com.tagger.service.TranslationService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * {@code ApiServer} is the main HTTP server for the Latent Tagger backend application.
 * It initializes and configures an {@link HttpServer} to listen for incoming HTTP requests
 * and dispatches them to various {@link HttpHandler} implementations based on the request path.
 *
 * <p>The server is configured to use virtual threads for handling requests, which improves
 * concurrency and resource utilization for I/O-bound operations. It also integrates
 * Cross-Origin Resource Sharing (CORS) filtering to allow requests from different origins.
 *
 * <p>This class is responsible for setting up the API endpoints for tag search, validation,
 * implications, translation, presets, history management, model status, and model download,
 * by injecting the necessary service dependencies into their respective handlers.
 *
 * <p>The server binds to a loopback address ("127.0.0.1") on an ephemeral port (0),
 * which means the operating system will assign a free port. The actual address can be
 * retrieved using {@link #getAddress()}.
 *
 * <p>Usage:
 * <pre>{@code
 * TagDatabaseService tagDb = new TagDatabaseService(...);
 * TranslationService transSvc = new TranslationService(...);
 * HistoryService histSvc = new HistoryService();
 * LlmModelService llmSvc = new LlmModelService();
 *
 * ApiServer apiServer = new ApiServer(tagDb, transSvc, histSvc, llmSvc);
 * apiServer.start();
 * System.out.println("Server started on: " + apiServer.getAddress());
 * }</pre>
 *
 * @see HttpServer
 * @see HttpHandler
 * @see CorsFilter
 * @see TagSearchHandler
 * @see TagValidateHandler
 * @see ImplicationsHandler
 * @see TranslateHandler
 * @see PresetsHandler
 * @see HistoryHandler
 * @see ShutdownHandler
 * @see ModelStatusHandler
 * @see ModelDownloadHandler
 * @see ModelPathHandler
 */
public class ApiServer {

    private final HttpServer server;

    public ApiServer(TagDatabaseService tagDb, TranslationService transSvc, HistoryService histSvc, LlmModelService llmSvc) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        createContexts(tagDb, transSvc, histSvc, llmSvc);
    }

    private void createContexts(TagDatabaseService tagDb, TranslationService transSvc, HistoryService histSvc, LlmModelService llmSvc) {
        server.createContext("/api/tags/search", withCors(new TagSearchHandler(tagDb)));
        server.createContext("/api/tags/validate", withCors(new TagValidateHandler(tagDb)));
        server.createContext("/api/tags/implications", withCors(new ImplicationsHandler(tagDb)));
        server.createContext("/api/translate", withCors(new TranslateHandler(transSvc)));
        server.createContext("/api/presets", withCors(new PresetsHandler()));
        server.createContext("/api/history", withCors(new HistoryHandler(histSvc)));
        server.createContext("/api/shutdown", withCors(new ShutdownHandler()));
        server.createContext("/api/model/status", withCors(new ModelStatusHandler(llmSvc)));
        server.createContext("/api/model/download", withCors(new ModelDownloadHandler(llmSvc)));
        server.createContext("/api/model/path", withCors(new ModelPathHandler(llmSvc)));
    }

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

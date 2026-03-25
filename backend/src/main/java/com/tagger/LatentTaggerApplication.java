package com.tagger;

import com.tagger.data.TagDataLoader;
import com.tagger.server.ApiServer;
import com.tagger.service.HistoryService;
import com.tagger.service.LlmModelService;
import com.tagger.service.TagDatabaseService;
import com.tagger.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * {@code LatentTaggerApplication} is the entry point for the Latent Tagger backend application.
 * This class initializes and orchestrates the various services and components required
 * for the application to function, including:
 * <ul>
 *     <li>Loading tag data, aliases, and implications into {@link TagDatabaseService}.</li>
 *     <li>Setting up the {@link TranslationService} for natural language to tag translation.</li>
 *     <li>Initializing the {@link HistoryService} for persistent storage of translation history.</li>
 *     <li>Managing the {@link LlmModelService} for downloading and monitoring the LLM.</li>
 *     <li>Starting the {@link ApiServer} to expose RESTful endpoints for client interaction.</li>
 * </ul>
 *
 * <p>The application generates a unique authentication token at startup, which is used
 * to secure various API endpoints. It also writes a temporary file containing the
 * server's port and authentication token, facilitating client discovery. This file
 * is automatically cleaned up on application shutdown.
 *
 * <p>The main thread is kept alive indefinitely to allow the HTTP server to operate.
 *
 * @see TagDatabaseService
 * @see TranslationService
 * @see HistoryService
 * @see LlmModelService
 * @see ApiServer
 */
public class LatentTaggerApplication {

    private static final Logger log = LoggerFactory.getLogger(LatentTaggerApplication.class);
    public static final String AUTH_TOKEN = UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        TagDatabaseService tagDatabaseService = TagDataLoader.load();
        long elapsed = System.currentTimeMillis() - start;

        log.info(String.format("[Latent Tagger] Loaded %,d tags, %,d aliases, and %,d implications in %dms",
                tagDatabaseService.getTagCount(),
                tagDatabaseService.getAliasCount(),
                tagDatabaseService.getImplicationCount(),
                elapsed));

        TranslationService translationService = new TranslationService(tagDatabaseService);
        HistoryService historyService = new HistoryService();
        LlmModelService llmModelService = new LlmModelService();

        ApiServer server = new ApiServer(tagDatabaseService, translationService, historyService, llmModelService);
        server.start();

        int port = server.getAddress().getPort();
        String portAndToken = port + ":" + AUTH_TOKEN;

        Path portFile = Paths.get(System.getProperty("java.io.tmpdir"), ".tagger-port");
        try {
            Files.writeString(portFile, portAndToken);
        } catch (IOException e) {
            log.error("Failed to write port file", e);
            System.exit(1);
        }

        System.out.println("TAGGER_PORT=" + portAndToken);
        log.info("Tagger backend started on port {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.deleteIfExists(portFile);
                log.info("Port file deleted.");
            } catch (IOException e) {
                log.warn("Failed to delete port file on shutdown.", e);
            }
        }));

        Thread.sleep(Long.MAX_VALUE);
    }
}

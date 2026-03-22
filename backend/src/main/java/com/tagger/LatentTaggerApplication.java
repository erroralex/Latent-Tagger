package com.tagger;

import com.tagger.data.TagDataLoader;
import com.tagger.server.ApiServer;
import com.tagger.service.HistoryService;
import com.tagger.service.TagDatabaseService;
import com.tagger.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

        ApiServer server = new ApiServer(tagDatabaseService, translationService, historyService);
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

        // Keep main thread alive so the server can run
        Thread.sleep(Long.MAX_VALUE);
    }
}

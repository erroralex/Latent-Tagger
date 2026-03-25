package com.tagger.service;

import com.tagger.model.HistoryEntry;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@code HistoryService} manages the persistence and retrieval of translation history entries.
 * It uses an SQLite database to store records of user queries, generated tags,
 * the model used for translation, and the timestamp of the operation.
 *
 * <p>The database file is created and managed in the user's home directory under {@code .tagger/history.db}.
 * This service ensures that the database schema is initialized upon instantiation if it doesn't already exist.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Saving new translation history entries, including the input natural language text,
 *         the resulting tags, and the model identifier.</li>
 *     <li>Retrieving a list of recent history entries, ordered by creation time.</li>
 * </ul>
 *
 * <p>Error handling for database operations is implemented to log issues to standard error,
 * preventing application crashes due to database problems.
 *
 * @see HistoryEntry
 */
public class HistoryService {

    private final String dbUrl;

    public HistoryService() {
        String dbPath = System.getProperty("user.home") + "/.tagger/history.db";
        File dbFile = new File(dbPath);
        dbFile.getParentFile().mkdirs();
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initDb();
    }

    private void initDb() {
        String sql = "CREATE TABLE IF NOT EXISTS prompt_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tags TEXT NOT NULL," +
                "nl_input TEXT," +
                "model TEXT," +
                "created_at INTEGER NOT NULL" +
                ");";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed to initialize history database: " + e.getMessage());
        }
    }

    public void save(List<String> tags, String nlInput, String model) {
        String sql = "INSERT INTO prompt_history(tags, nl_input, model, created_at) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.join(",", tags));
            pstmt.setString(2, nlInput);
            pstmt.setString(3, model);
            pstmt.setLong(4, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save history entry: " + e.getMessage());
        }
    }

    public List<HistoryEntry> getRecent(int limit) {
        String sql = "SELECT * FROM prompt_history ORDER BY created_at DESC LIMIT ?";
        List<HistoryEntry> entries = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                entries.add(new HistoryEntry(
                        rs.getLong("id"),
                        Arrays.asList(rs.getString("tags").split(",")),
                        rs.getString("nl_input"),
                        rs.getString("model"),
                        rs.getLong("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get history entries: " + e.getMessage());
            return Collections.emptyList();
        }
        return entries;
    }
}

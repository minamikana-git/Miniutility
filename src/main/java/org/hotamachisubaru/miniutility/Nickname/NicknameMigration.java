package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

public class NicknameMigration {
    private final File yamlFile;
    private final String dbPath;
    private final Logger logger;

    public NicknameMigration(File yamlFile, String dbPath, Logger logger) {
        this.yamlFile = yamlFile;
        this.dbPath = dbPath;
        this.logger = logger;
    }

    public void migrateToDatabase() {
        if (!yamlFile.exists()) {
            logger.warning("ニックネームの保存ファイルがありません。統合をスキップします。");
            return;
        }

        // Load YAML file
        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        Set<String> keys = yamlConfig.getKeys(false);

        if (keys.isEmpty()) {
            logger.info("ニックネームが存在しません。もしくは壊れています。 統合をスキップします。");
            return;
        }

        // Connect to the database
        String dbUrl = "jdbc:sqlite:" + Path.of(dbPath).toAbsolutePath();
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String insertQuery = "REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (String uuid : keys) {
                    String nickname = yamlConfig.getString(uuid);
                    if (nickname != null) {
                        pstmt.setString(1, uuid);
                        pstmt.setString(2, nickname);
                        pstmt.executeUpdate();
                        logger.info("データベースへのニックネームの統合に成功しました。" + uuid);
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("データベースへのニックネームの統合に失敗しました: " + e.getMessage());

        }
    }
}
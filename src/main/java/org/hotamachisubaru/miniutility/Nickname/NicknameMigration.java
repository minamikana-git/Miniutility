package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

import java.io.File;
import java.sql.*;
import java.util.Set;
import java.util.logging.Logger;

/**
 * ニックネームYAML→SQLite移行用
 * データベースパスやログ取得はMiniutilityLoaderから
 */
public class NicknameMigration {

    private final MiniutilityLoader plugin;

    public NicknameMigration(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    /**
     * YAMLファイルからSQLiteデータベースへニックネームを移行
     */
    public void migrateToDatabase() {
        File yamlFile = new File(plugin.getDataFolder(), "nickname.yml");
        String dbPath = new File(plugin.getDataFolder(), "nicknames.db").getPath();
        Logger logger = plugin.getLogger();

        if (!yamlFile.exists()) {
            logger.info("ニックネームの保存ファイルがありません。統合をスキップします。");
            return;
        }

        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        Set<String> keys = yamlConfig.getKeys(false);
        if (keys == null || keys.isEmpty()) {
            logger.warning("ニックネームが存在しません。もしくは壊れています。統合をスキップします。");
            return;
        }

        String dbUrl = "jdbc:sqlite:" + new File(dbPath).getAbsolutePath();
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            // テーブルなければ作成
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nicknames (uuid TEXT PRIMARY KEY, nickname TEXT)");
            }

            String insertQuery = "REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (String uuid : keys) {
                    String nickname = yamlConfig.getString(uuid);
                    if (nickname != null) {
                        pstmt.setString(1, uuid);
                        pstmt.setString(2, nickname);
                        pstmt.executeUpdate();
                        logger.info("データベースへのニックネームの統合に成功しました: " + uuid);
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("データベースへのニックネームの統合に失敗しました: " + e.getMessage());
        }
    }
}

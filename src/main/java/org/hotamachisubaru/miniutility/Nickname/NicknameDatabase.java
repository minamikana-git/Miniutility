
package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.ChatColor;

import java.nio.file.Paths;
import java.sql.*;
import java.util.logging.Logger;

public class NicknameDatabase {
    private Connection connection;
    private static String path;
    private static final String DB_URL = "jdbc:sqlite:plugins/Miniutility/nickname.db";
    private static final Logger logger = Logger.getLogger("Database");

    /**
     * コンストラクタ
     * @param path プラグインデータフォルダのパス
     */
    public NicknameDatabase(String path) {
        NicknameDatabase.path = Paths.get(path).toAbsolutePath().toString();
    }

    /**
     * データベースセットアップ（接続とテーブル作成）
     */
    public void setupDatabase() {
        try {
            openConnection();
            logger.info("データベースのセットアップが正常に完了しました。");
        } catch (SQLException e) {
            logger.severe("データベースのセットアップに失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * テーブルを作成（存在しない場合のみ）
     */
    private void createTables() throws SQLException {
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS nicknames (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "nickname TEXT" +
                        ");";
                stmt.execute(sql);
                logger.info("ニックネームテーブルを作成しました（既に存在する場合もあります）。");
            }
        }
    }

    /**
     * データベース接続を開き、テーブルを作成する
     */
    public void openConnection() throws SQLException {
        String url = "jdbc:sqlite:" + path + "/nickname.db";
        connection = DriverManager.getConnection(url);
        createTables();
    }

    /**
     * データベース接続を閉じる
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("データベース接続を閉じることができませんでした: " + e.getMessage());
        }
    }

    /**
     * ニックネームを保存（REPLACE INTO）
     */
    public static void saveNickname(String uuid, String nickname) {
        String formattedNickname = ChatColor.translateAlternateColorCodes('&', nickname);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?);"
             )) {
            stmt.setString(1, uuid);
            stmt.setString(2, formattedNickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("ニックネームの保存に失敗しました: " + e.getMessage());
        }
    }

    /**
     * ニックネームを取得
     */
    public static String getNickname(String uuid) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT nickname FROM nicknames WHERE uuid = ?;"
             )) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nickname");
                }
            }
        } catch (SQLException e) {
            logger.warning("ニックネームの取得に失敗しました: " + e.getMessage());
        }
        return null;
    }
}

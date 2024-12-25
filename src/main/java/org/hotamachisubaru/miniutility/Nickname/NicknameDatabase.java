package org.hotamachisubaru.miniutility.Nickname;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class NicknameDatabase {

    private static String path;
    private Connection connection;
    private static final Logger logger = Logger.getLogger("NicknameDatabase");

    public NicknameDatabase(String path) {
        NicknameDatabase.path = path;
    }

    public static void saveNicknameToDatabase(String uuid, String nickname) {
        String query = "REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?);";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path + "/nickname.db");
             var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, nickname);
            pstmt.executeUpdate();
            logger.info("UUID: " + uuid + " のニックネームをデータベースに保存しました。");
        } catch (SQLException e) {
            logger.severe("UUID: " + uuid + " のニックネームをデータベースに保存できませんでした: " + e.getMessage());
        }
    }

    public static String loadNicknameFromDatabase(String uuid) {
        String query = "SELECT nickname FROM nicknames WHERE uuid = ?;";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path + "/nickname.db");
             var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, uuid);
            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nickname");
                }
            }
        } catch (SQLException e) {
            logger.severe("UUID: " + uuid + " のニックネームをデータベースから取得できませんでした: " + e.getMessage());
        }
        return null;
    }

    public void openConnection() throws SQLException {
        String url = "jdbc:sqlite:" + path + "/nickname.db"; // URL修正
        connection = DriverManager.getConnection(url);
        createTables();
    }

    public void setupDatabase() {
        try {
            openConnection(); // データベース接続を開く
            createTables(); // 必要なテーブルを作成
            logger.info("データベースのセットアップが正常に完了しました。");
        } catch (SQLException e) {
            logger.severe("データベースのセットアップに失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        if (connection != null) {
            try (Statement statement = connection.createStatement()) {
                String nicknamesTableQuery = """
                CREATE TABLE IF NOT EXISTS nicknames (
                    uuid TEXT PRIMARY KEY,
                    nickname TEXT
                );
                """;
                statement.execute(nicknamesTableQuery);
                logger.info("ニックネームテーブルを作成しました（既に存在する場合もあります）。");
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("データベース接続を閉じました。");
            }
        } catch (SQLException e) {
            logger.severe("データベース接続を閉じることができませんでした: " + e.getMessage());
        }
    }
}

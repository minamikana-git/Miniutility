package org.hotamachisubaru.miniutility.Nickname;

import java.sql.*;
import java.util.logging.Logger;

public class NicknameDatabase {

    private static final String DB_URL = "jdbc:sqlite:plugins/Miniutility/nickname.db";
    private static String path;
    private Connection connection;
    private static final Logger logger = Logger.getLogger("NicknameDatabase");
    public NicknameDatabase(String path){
        NicknameDatabase.path = path;
    }

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTable = "CREATE TABLE IF NOT EXISTS nicknames (uuid TEXT PRIMARY KEY, nickname TEXT);";
            conn.createStatement().execute(createTable);
        } catch (SQLException e) {
            logger.severe("データベースの接続に失敗しました。");
        }
    }




    public static void saveNickname(String uuid, String nickname) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?);")) {
            stmt.setString(1, uuid);
            stmt.setString(2, nickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("ニックネームの保存に失敗しました。");
        }
    }

    public static String getNickname(String uuid) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT nickname FROM nicknames WHERE uuid = ?;")) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            logger.warning("ニックネームの取得に失敗しました。ファイルが破損している可能性があります。");
        }
        return null;
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
    public void openConnection() throws SQLException {
        String url = "jdbc:sqlite:" + path + "/nickname.db"; // URL修正
        connection = DriverManager.getConnection(url);
        createTables();
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



package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.logging.Logger;

/**
 * ニックネームデータベース管理（Paper全バージョン共通設計）
 */
public class NicknameDatabase {
    private static final Logger logger = Bukkit.getLogger();
    private static final String DB_URL = "jdbc:sqlite:plugins/Miniutility/nickname.db";

    public static void init() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nickname (" +
                    "uuid TEXT PRIMARY KEY," +
                    "nickname TEXT NOT NULL)");
        } catch (SQLException e) {
            logger.warning("ニックネームDBの初期化に失敗: " + e.getMessage());
        }
    }

    public static void saveNickname(Player player, String nickname) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO nickname (uuid, nickname) VALUES (?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, nickname);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("ニックネーム保存失敗: " + e.getMessage());
        }
    }

    public static String loadNickname(Player player) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nickname FROM nickname WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nickname");
                }
            }
        } catch (SQLException e) {
            logger.warning("ニックネーム取得失敗: " + e.getMessage());
        }
        return null;
    }

    public static void deleteNickname(Player player) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM nickname WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("ニックネーム削除失敗: " + e.getMessage());
        }
    }
}

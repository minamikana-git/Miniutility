package org.hotamachisubaru.miniutility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.hotamachisubaru.miniutility.Command.CommandManager;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.Nickname.NicknameMigration;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Miniutility メインクラス (Paper/Folia完全両対応・1.21.8最適化)
 */
public class Miniutility {

    private final MiniutilityLoader plugin; // JavaPlugin参照
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();
    private final Logger logger;
    private NicknameDatabase nicknameDatabase;
    private NicknameManager nicknameManager;
    private CreeperProtectionListener creeperProtectionListener;
    private Chat chatListener;
    private final PluginManager pm;

    public Miniutility(MiniutilityLoader plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pm = plugin.getServer().getPluginManager();
    }

    public void enable() {
        plugin.saveDefaultConfig();
        setupDatabase();
        nicknameDatabase = new NicknameDatabase(plugin);
        nicknameManager = new NicknameManager(plugin, nicknameDatabase);
        creeperProtectionListener = new CreeperProtectionListener(plugin);
        chatListener = new Chat(plugin, nicknameDatabase, nicknameManager);
        registerListeners();
        var cmd = new CommandManager(plugin);
        plugin.getCommand("menu").setExecutor(cmd);
        plugin.getCommand("menu").setTabCompleter(cmd);
        plugin.getCommand("load").setExecutor(cmd);
        plugin.getCommand("load").setTabCompleter(cmd);
        plugin.getCommand("prefixtoggle").setExecutor(cmd);
        plugin.getCommand("prefixtoggle").setTabCompleter(cmd);
        checkLuckPerms();
        // マイグレーション
        NicknameMigration migration = new NicknameMigration(plugin);
        migration.migrateToDatabase();
        checkUpdates();
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    public void disable() {
        if (nicknameDatabase != null) {
            nicknameDatabase.saveAll();
        }
    }

    private void registerListeners() {
        pm.registerEvents(new DeathListener(plugin), plugin);
        pm.registerEvents(chatListener, plugin);
        pm.registerEvents(creeperProtectionListener, plugin);
        pm.registerEvents(new Menu(plugin), plugin);
        pm.registerEvents(new NicknameListener(plugin, nicknameManager), plugin);
        pm.registerEvents(new TrashListener(plugin), plugin);
    }

    private void checkUpdates() {
        String owner = "minamikana-git";
        String repo = "Miniutility";
        String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/releases/latest",
                owner, repo
        );
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warning("アップデートのチェックに失敗しました: HTTP " + response.statusCode());
                return;
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String latestTag = json.get("tag_name").getAsString().replaceFirst("^v", "");
            String currentVersion = plugin.getDescription().getVersion();
            if (!currentVersion.equals(latestTag)) {
                String url = json.get("html_url").getAsString();
                String msg = "新しいバージョン "
                        + latestTag + " が利用可能です！ ダウンロード: " + url;
                logger.info(msg);
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (p.isOp()) {
                        FoliaUtil.runAtPlayer(plugin, p, () -> p.sendMessage(msg));
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            logger.warning("アップデートのチェック中にエラーが発生しました: " + e.getMessage());
        }
    }

    private void checkLuckPerms() {
        if (pm.getPlugin("LuckPerms") == null) {
            logger.warning("LuckPermsが見つかりません。Prefixなしで続行します。");
        }
    }

    private void migration() {
        File flag = new File(plugin.getDataFolder(), "migrationCompleted.flag");
        if (flag.exists()) return;

        File oldFile = new File(plugin.getDataFolder(), "nickname.yml");
        if (!oldFile.exists()) return;

        try {
            YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            NicknameDatabase db = new NicknameDatabase(plugin);
            for (String key : oldConfig.getKeys(false)) {
                String name = oldConfig.getString(key);
                if (name != null) db.setNickname(key, name);
            }
            oldFile.renameTo(new File(plugin.getDataFolder(), "nickname.yml.bak"));
            flag.createNewFile();
        } catch (Exception e) {
            logger.severe("マイグレーション失敗: " + e.getMessage());
        }
    }

    private void setupDatabase() {
        File dbFile = new File(plugin.getDataFolder(), "nickname.db");
        if (dbFile.exists()) {
            logger.info("nickname.dbが既に存在します。初期セットアップをスキップします。");
            return;
        }
        try {
            String dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            try (java.sql.Connection connection = java.sql.DriverManager.getConnection(dbUrl)) {
                try (java.sql.Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nicknames (uuid TEXT PRIMARY KEY, nickname TEXT)");
                }
            }
            logger.info("nickname.dbを新規作成し、テーブルをセットアップしました。");
        } catch (Exception e) {
            logger.severe("nickname.db初期セットアップに失敗: " + e.getMessage());
        }
    }

    // 死亡地点管理
    public void setDeathLocation(UUID uuid, Location loc) {
        deathLocations.put(uuid, loc);
    }

    public Location getDeathLocation(UUID uuid) {
        return deathLocations.get(uuid);
    }

    // サブシステム取得
    public NicknameDatabase getNicknameDatabase() {
        return nicknameDatabase;
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public CreeperProtectionListener getCreeperProtectionListener() {
        return creeperProtectionListener;
    }



    public Chat getChatListener() {
        return chatListener;
    }
}

package org.hotamachisubaru.miniutility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.hotamachisubaru.miniutility.Command.CommandManager;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.Nickname.NicknameMigration;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getOnlinePlayers;

/**
 * Miniutility メインクラス (Paper対応)
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
    private volatile String lastNotifiedVersion = null;
    private static final int HTTP_TIMEOUT_MS = 7000;


    public Miniutility(MiniutilityLoader plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pm = plugin.getServer().getPluginManager();
    }

    public void enable() {
        plugin.saveDefaultConfig();
        setupDatabase();
        nicknameDatabase = new NicknameDatabase();
        nicknameManager = new NicknameManager(this.nicknameDatabase);
        creeperProtectionListener = new CreeperProtectionListener();
        chatListener = new Chat();
        registerListeners();
        CommandManager cmd = new CommandManager(plugin);
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
        UpdateCheck();
        scheduleDailyUpdateCheck();
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    public void disable() {
        if (nicknameDatabase != null) {
            nicknameDatabase.saveAll();
        }
    }

    private void registerListeners() {
        pm.registerEvents(chatListener, plugin);
        pm.registerEvents(creeperProtectionListener, plugin);
        pm.registerEvents(new DeathListener(this), plugin);
        pm.registerEvents(new Menu(plugin), plugin);
        pm.registerEvents(new NicknameListener(plugin, nicknameManager), plugin);
        pm.registerEvents(new TrashListener(plugin), plugin);
        pm.registerEvents(new ChatPaperListener(), plugin);
    }

    private void UpdateCheck() {
        final String owner = "minamikana-git";
        final String repo = "Miniutility";
        final String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest";

        CompletableFuture.supplyAsync(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(HTTP_TIMEOUT_MS))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofMillis(HTTP_TIMEOUT_MS))
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "Miniutility/" + plugin.getDescription().getVersion())
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return new HttpResp(response.statusCode(), response.body());
            } catch (Exception e) {
                logger.warning("アップデートのチェック中にエラーが発生しました: " + e.getMessage());
                return new HttpResp(-1, null);
            }
        }).thenAccept(resp -> {
            if (resp == null || resp.code() != 200 || resp.body() == null) {
                if (resp != null && resp.code() != 200) {
                    logger.warning("アップデートのチェックに失敗しました: HTTP " + resp.code());
                }
                return;
            }
            try {
                JsonObject json = new JsonParser().parse(resp.body()).getAsJsonObject();
                String latestTag = json.get("tag_name").getAsString().replaceFirst("^v", "");
                String url = json.get("html_url").getAsString();
                String currentVersion = plugin.getDescription().getVersion();

                if (!currentVersion.equals(latestTag) && !latestTag.equals(lastNotifiedVersion)) {
                    String msg = "新しいバージョン " + latestTag + " が利用可能です！ ダウンロード: " + url;
                    logger.info(msg);
                    lastNotifiedVersion = latestTag;

                    // Folia安全にOPへ通知
                    for (Player p : getOnlinePlayers()) {
                        if (p.isOp()) {
                            FoliaUtil.runAtPlayer(
                                    plugin, p.getUniqueId(), () -> p.sendMessage(msg)
                            );
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warning("アップデート情報の解析に失敗しました: " + ex.getMessage());
            }
        });
    }

    // ヘルパー：HTTPレスポンス保持
        private record HttpResp(int code, String body) {
    }

    private void checkLuckPerms() {
        if (pm.getPlugin("LuckPerms") == null) {
            logger.info("LuckPermsが見つかりません。Prefixなしで続行します。");
        }
    }

    private void migration() {
        File flag = new File(plugin.getDataFolder(), "migrationCompleted.flag");
        if (flag.exists()) return;

        File oldFile = new File(plugin.getDataFolder(), "nickname.yml");
        if (!oldFile.exists()) return;

        try {
            YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            NicknameDatabase db = new NicknameDatabase();
            for (String key : oldConfig.getKeys(false)) {
                String name = oldConfig.getString(key);
                if (name != null) db.setNickname(key, name);
            }
            oldFile.renameTo(new File(plugin.getDataFolder(), "nickname.yml.bak"));
            flag.createNewFile();
        } catch (Exception e) {
            logger.warning("マイグレーションに失敗しました: " + e.getMessage());
        }
    }

    private void setupDatabase() {
        // data フォルダを必ず作成
        File dataDir = plugin.getDataFolder();
        if (!dataDir.exists()) dataDir.mkdirs();

        File dbFile = new File(dataDir, "nickname.db");
        String dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try (java.sql.Connection con = java.sql.DriverManager.getConnection(dbUrl);
             java.sql.Statement st = con.createStatement()) {

            // 既存でも毎回実行してOK
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS nickname (" +
                            "uuid TEXT PRIMARY KEY," +
                            "name TEXT NOT NULL," +           // ニックネーム
                            "color TEXT," +                   // カラー(任意)
                            "show_prefix INTEGER DEFAULT 1" + // Prefix表示フラグ(任意)
                            ")"
            );
        } catch (Exception e) {
            logger.severe("nickname.db 初期セットアップに失敗しました: " + e.getMessage());
        }
    }

    private void scheduleDailyUpdateCheck() {
        // 0.5秒後に初回チェック
        FoliaUtil.runLater(
                plugin,
                this::safeUpdateCheckAndReschedule,
                10L
        );
    }

    private void safeUpdateCheckAndReschedule() {
        try {
            UpdateCheck(); // あなたの非同期版/同期版どちらでもOK（非同期版推奨）
        } catch (Throwable t) {
            logger.warning("アップデートのチェックに失敗しました：" + t.getMessage());
        } finally {
            // 1日後(= 20t * 60s * 60m * 24h)に再チェック
            long oneDayTicks = 20L * 60L * 60L * 24L;
            FoliaUtil.runLater(
                    plugin,
                    this::safeUpdateCheckAndReschedule,
                    oneDayTicks
            );
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


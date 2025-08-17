package org.hotamachisubaru.miniutility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Miniutility メインクラス (Paper/legacy完全両対応)
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
        // 旧式チャットは常に
        pm.registerEvents(chatListener, plugin);

        // 新式(Paper 1.19+ の AsyncChatEvent)は存在する時だけ
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            Object modern = Class.forName("org.hotamachisubaru.miniutility.Bridge.ChatPaperListener")
                    .getDeclaredConstructor().newInstance();
            pm.registerEvents((org.bukkit.event.Listener) modern, plugin);
        } catch (ClassNotFoundException ignore) {
            // 1.17.1 など新式なし
        } catch (ReflectiveOperationException re) {
            logger.warning("エラーが発生しました: " + re.getMessage());
        }

        pm.registerEvents(creeperProtectionListener, plugin);
        pm.registerEvents(new DeathListener(this), plugin);
        pm.registerEvents(new Menu(plugin), plugin);
        pm.registerEvents(new NicknameListener(plugin, nicknameManager), plugin);
        pm.registerEvents(new TrashListener(plugin), plugin);
    }

    private void UpdateCheck() {
        String owner = "minamikana-git";
        String repo  = "Miniutility";
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", owner, repo);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Miniutility/" + plugin.getDescription().getVersion())
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        logger.warning("アップデートのチェックに失敗しました: HTTP " + response.statusCode());
                        return;
                    }
                    try {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        String latestTag = json.get("tag_name").getAsString().replaceFirst("^v", "");
                        String currentVersion = plugin.getDescription().getVersion();

                        if (!currentVersion.equals(latestTag)) {
                            String url = json.get("html_url").getAsString();
                            String msg = "新しいバージョン " + latestTag + " が利用可能です！ ダウンロード: " + url;
                            logger.info(msg);
                            lastNotifiedVersion = latestTag;

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.isOp()) {
                                    FoliaUtil.runAtPlayer(plugin, p.getUniqueId(), () -> p.sendMessage(msg));
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.warning("アップデート情報の解析に失敗しました: " + ex.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    logger.warning("アップデートのチェック中にエラーが発生しました: " + ex.getMessage());
                    return null;
                });
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
            NicknameDatabase db = new NicknameDatabase();
            for (String key : oldConfig.getKeys(false)) {
                String name = oldConfig.getString(key);
                if (name != null) db.setNickname(key, name);
            }
            oldFile.renameTo(new File(plugin.getDataFolder(), "nickname.yml.bak"));
            flag.createNewFile();
        } catch (Exception e) {
            logger.severe("マイグレーションに失敗しました: " + e.getMessage());
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
            logger.severe("nickname.db初期セットアップに失敗しました: " + e.getMessage());
        }
    }

    private void scheduleDailyUpdateCheck() {
        // 0.5秒後に初回チェック
        org.hotamachisubaru.miniutility.util.FoliaUtil.runLater(
                plugin,
                this::safeUpdateCheckAndReschedule,
                10L
        );
    }

    private void safeUpdateCheckAndReschedule() {
        try {
            UpdateCheck(); // あなたの非同期版/同期版どちらでもOK（非同期版推奨）
        } catch (Throwable t) {
            plugin.getLogger().warning("アップデートのチェックに失敗しました：" + t.getMessage());
        } finally {
            // 1日後(= 20t * 60s * 60m * 24h)に再チェック
            long oneDayTicks = 20L * 60L * 60L * 24L;
            org.hotamachisubaru.miniutility.util.FoliaUtil.runLater(
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

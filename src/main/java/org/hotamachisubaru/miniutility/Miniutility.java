package org.hotamachisubaru.miniutility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.Load;
import org.hotamachisubaru.miniutility.Command.TogglePrefixCommand;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.jetbrains.annotations.Nullable;

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

public class Miniutility extends JavaPlugin {
    private Chat chatListener;
    private NicknameManager nicknameManager;
    private CreeperProtectionListener creeperProtectionListener;
    private final Logger logger = getLogger();
    private final PluginManager pm = getServer().getPluginManager();
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        // --- 1. リソースと設定 ---
        File dbFile = new File(getDataFolder(),"nickname.db");
        if (!dbFile.exists()) {
            saveResource("nickname.db", false);
        } else {
            logger.info("nickname.dbが既に存在するため、保存をスキップします。");
        }
        saveDefaultConfig();
        // --- 2. 依存チェック ---
        checkLuckPerms();
        // --- 3. データベースセットアップ（テーブル作成は一度だけ） ---
        setupDatabase();
        // --- 4. マイグレーション処理 ---
        migration();
        // --- 5. インスタンス生成 ---
        chatListener = new Chat(this);
        creeperProtectionListener = new CreeperProtectionListener();
        nicknameManager = new NicknameManager();
        // --- 6. リスナー登録 ---
        registerListeners();
        // --- 7. コマンド登録 ---
        registerCommands();
        // --- 8. アップデートチェック（非同期のみ） ---
        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkUpdates);
        // --- 9. 開発者情報 ---
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    @Override
    public void onDisable() {
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }



    private void checkUpdates() {
        String owner = "minamikana-git";
        String repo  = "Miniutility";
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
            String currentVersion = getDescription().getVersion();
            if (!currentVersion.equals(latestTag)) {
                String url = json.get("html_url").getAsString();
                String msg = "新しいバージョン "
                        + latestTag + " が利用可能です！ ダウンロード: " + url;
                logger.info(msg);
                Bukkit.getScheduler().runTask(this, () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.isOp()) p.sendMessage(msg);
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
        File flag = new File(getDataFolder(), "migrationCompleted.flag");
        if (flag.exists()) return;

        File oldFile = new File(getDataFolder(), "nickname.yml");
        if (!oldFile.exists()) return;

        try {
            YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            NicknameDatabase db = new NicknameDatabase(getDataFolder().getAbsolutePath());
            for (String key : oldConfig.getKeys(false)) {
                String name = oldConfig.getString(key);
                if (name != null) NicknameDatabase.saveNickname(key, name);
            }
            oldFile.renameTo(new File(getDataFolder(), "nickname.yml.bak"));
            flag.createNewFile();
        } catch (Exception e) {
            logger.severe("マイグレーション失敗: " + e.getMessage());
        }
    }

    private void setupDatabase() {
        try {
            NicknameDatabase db = new NicknameDatabase(getDataFolder().getAbsolutePath());
            db.setupDatabase();
            logger.info("データベースの設定が完了しました。");
        } catch (Exception e) {
            logger.severe("データベースセットアップ失敗: " + e.getMessage());
        }
    }

    private void registerListeners() {
        pm.registerEvents(new DeathListener(this),this);
        pm.registerEvents(chatListener, this);
        pm.registerEvents(creeperProtectionListener, this);
        pm.registerEvents(new Menu(), this);
        pm.registerEvents(new NicknameClickListener(), this);
        pm.registerEvents(new TrashClickListener(this), this);
        pm.registerEvents(new Utility(), this);
        pm.registerEvents(nicknameManager, this);
    }

    private void registerCommands() {
        getCommand("menu").setExecutor(new UtilityCommand());
        getCommand("load").setExecutor(new Load());
        getCommand("prefixtoggle").setExecutor(new TogglePrefixCommand(this));
    }

    public CreeperProtectionListener getCreeperProtectionListener() {
        return creeperProtectionListener;
    }

    public void setDeathLocation(UUID uuid, Location loc) {
        deathLocations.put(uuid,loc);
    }

    @Nullable
    public Location getDeathLocation(UUID uuid) {
        return deathLocations.get(uuid);
    }


}

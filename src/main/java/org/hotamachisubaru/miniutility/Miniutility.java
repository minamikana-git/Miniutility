package org.hotamachisubaru.miniutility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.Load;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.logging.Logger;

public class Miniutility extends JavaPlugin {
    private Chat chatListener;
    private NicknameManager nicknameManager;
    private CreeperProtectionListener creeperProtectionListener;
    private final Logger logger = getLogger();
    private final PluginManager pm = getServer().getPluginManager();

    @Override
    public void onEnable() {
        // --- 0. 起動ログ ---
        logger.info("Enabling Miniutility v" + getDescription().getVersion());

        // --- 1. nickname.db を初回のみ保存 ---
        File dbFile = new File(getDataFolder(), "nickname.db");
        if (!dbFile.exists()) {
            saveResource("nickname.db", false);
            logger.info("デフォルト nickname.db を保存しました。");
        }

        // --- 2. コンフィグと依存チェック ---
        saveDefaultConfig();
        checkLuckPerms();

        // --- 3. マイグレーション処理 ---
        migration();

        // --- 4. データベースセットアップ ---
        setupDatabase();

        // --- 5. リスナー登録 ---
        registerListeners();

        // --- 6. コマンド登録 ---
        registerCommands();

        // --- 7. アップデートチェック（非同期） ---
        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkUpdates);

        // --- 8. 開発者情報 ---
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    /** GitHub リリースから最新バージョンを取得し、OP に通知します */
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
                logger.warning("アップデートチェック失敗: HTTP " + response.statusCode());
                return;
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String latestTag = json.get("tag_name").getAsString().replaceFirst("^v", "");
            String currentVersion = getDescription().getVersion();
            if (!currentVersion.equals(latestTag)) {
                String url = json.get("html_url").getAsString();
                String msg = NamedTextColor.AQUA + "[Miniutility] 新しいバージョン "
                        + latestTag + " が利用可能です！ ダウンロード: " + url;
                logger.info(msg);
                Bukkit.getScheduler().runTask(this, () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.isOp()) p.sendMessage(msg);
                    }
                });
            } else {
                logger.fine("Miniutility は最新バージョン(" + currentVersion + ")です。");
            }
        } catch (IOException | InterruptedException e) {
            logger.warning("アップデートチェック中にエラー: " + e.getMessage());
        }
    }

    /** LuckPerms の有無を確認し、なければ警告ログを出します */
    private void checkLuckPerms() {
        if (pm.getPlugin("LuckPerms") == null) {
            logger.warning("LuckPermsが見つかりません。デフォルト設定でPrefixなしで続行します。");
        }
    }

    /** 古い nickname.yml から DB へのマイグレーションを行い、完全済みなら以降スキップ */
    private void migration() {
        File flag = new File(getDataFolder(), "migrationCompleted.flag");
        if (flag.exists()) return;

        File oldFile = new File(getDataFolder(), "nickname.yml");
        if (!oldFile.exists()) return;

        try {
            YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            NicknameDatabase database = new NicknameDatabase(getDataFolder().getAbsolutePath());
            for (String key : oldConfig.getKeys(false)) {
                String name = oldConfig.getString(key);
                if (name != null) {
                    NicknameDatabase.saveNickname(key, name);
                    logger.info("ニックネーム移行: " + key);
                }
            }
            oldFile.renameTo(new File(getDataFolder(), "nickname.yml.bak"));
            flag.createNewFile();
        } catch (Exception e) {
            logger.severe("マイグレーション失敗: " + e.getMessage());
        }
    }

    /** SQLite DB を初期化し、テーブルを作成します */
    private void setupDatabase() {
        try {
            NicknameDatabase db = new NicknameDatabase(getDataFolder().getAbsolutePath());
            db.setupDatabase();
            logger.info("データベースの設定が完了しました。");
        } catch (Exception e) {
            logger.severe("データベースセットアップ失敗: " + e.getMessage());
        }
    }

    /** リスナーをまとめて登録 */
    private void registerListeners() {
        pm.registerEvents(chatListener = new Chat(this), this);
        pm.registerEvents(creeperProtectionListener = new CreeperProtectionListener(), this);
        pm.registerEvents(new Menu(), this);
        pm.registerEvents(new NicknameClickListener(), this);
        pm.registerEvents(new TrashClickListener(this), this);
        pm.registerEvents(new Utility(), this);
        pm.registerEvents(new NicknameManager(), this);
    }

    /** コマンドを登録 */
    private void registerCommands() {
        getCommand("menu").setExecutor(new UtilityCommand());
        getCommand("load").setExecutor(new Load());
    }

    @Override
    public void onDisable() {
        logger.info("Miniutility を無効化しました。");
    }

    public CreeperProtectionListener getCreeperProtectionListener() {
        return creeperProtectionListener;
    }

    public Object getDeathLocation(@NotNull UUID uniqueId) {
        return getServer().getOfflinePlayer(uniqueId).getLastDeathLocation();
    }
}

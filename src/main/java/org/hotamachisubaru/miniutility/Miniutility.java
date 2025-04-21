package org.hotamachisubaru.miniutility;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.Load;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

public class Miniutility extends JavaPlugin {
    private Chat chatListener;
    private NicknameManager nicknameManager;
    private final Logger logger = getLogger();
    private final PluginManager pm = getServer().getPluginManager();
    private CreeperProtectionListener creeperProtectionListener;


    @Override
    public void onEnable() {
        chatListener = new Chat(this); // チャットリスナー
        nicknameManager = new NicknameManager(); // ニックネーム管理
        creeperProtectionListener = new CreeperProtectionListener();
        saveResource("nickname.db", false); // デフォルトリソースを保存
        saveDefaultConfig(); // config.ymlの作成
        checkLuckPerms(); // LuckPermsがあるかチェック
        registerListeners(); // イベントリスナー登録
        migration(); // データ移行処理を実行
        setupDatabase(); // データベースセットアップ
        registerCommands(); // コマンド登録
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    private void checkLuckPerms() {
        if (pm.getPlugin("LuckPerms") == null) {
            logger.warning("LuckPermsが見つかりません。デフォルト設定でPrefixなしで続行します。");
        }
    }

    private void migration() {
        try {
            File migrationFlag = new File(getDataFolder(), "migrationCompleted.flag");
            if (migrationFlag.exists()) {
                logger.warning("ニックネームの統合は既に完了しています。スキップします。");
                return;
            }

            File oldFile = new File(getDataFolder(), "nickname.yml");
            if (!oldFile.exists()) {
                logger.info("統合するデータがありません。");
                return;
            }

            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            NicknameDatabase database = new NicknameDatabase(getDataFolder().getAbsolutePath());

            for (String key : oldConfig.getKeys(false)) {
                String nickname = oldConfig.getString(key);
                if (nickname != null) {
                    NicknameDatabase.saveNickname(key, nickname); // データベースに保存
                    logger.info("ニックネームが移行されました：" + key);
                }
            }

            if (oldFile.renameTo(new File(getDataFolder(), "nickname.yml.bak"))) {
                logger.info("古いnickname.ymlは、nickname.yml.bakとしてバックアップされました。");
            } else {
                logger.severe("nickname.ymlのバックアップに失敗しました。");
            }

            // 統合完了フラグを作成
            if (migrationFlag.createNewFile()) {
                logger.info("ニックネームの統合が完了し、フラグファイルが作成されました。");
            } else {
                logger.warning("フラグファイルの作成に失敗しました。");
            }
        } catch (Exception e) {
            logger.severe("ニックネームの統合に失敗しました: " + e.getMessage());
        }
    }

    private void setupDatabase() {
        try {
            NicknameDatabase database = new NicknameDatabase(getDataFolder().getAbsolutePath());
            database.setupDatabase();
            logger.info("データベースの設定が完了しました。");
        } catch (Exception e) {
            logger.severe("データベースの設定ができませんでした: " + e.getMessage());
        }
    }

    public void updateConfig(String key, Object value) {
        getConfig().set(key, value);
        saveConfig();
    }

    private void registerCommands() {
        getCommand("menu").setExecutor(new UtilityCommand());
        getCommand("load").setExecutor(new Load());

    }

    private void registerListeners() {
        pm.registerEvents(new Chat(this), this);
        pm.registerEvents(new CreeperProtectionListener(), this);
        pm.registerEvents(new Menu(), this);
        pm.registerEvents(new NicknameClickListener(), this);
        pm.registerEvents(new TrashClickListener(this), this);
        pm.registerEvents(new Utility(), this);
        pm.registerEvents(new NicknameManager(), this);

    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public Chat getChatListener() {
        return chatListener;
    }

    @Override
    public void onDisable() {
        logger.info("copyright 2024-2025 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    public CreeperProtectionListener getCreeperProtectionListener() {
        return creeperProtectionListener;
    }



    public Object getDeathLocation(@NotNull UUID uniqueId) {
        // Simulating death location retrieval (replace with actual implementation as needed)
        return getServer().getOfflinePlayer(uniqueId).getLastDeathLocation();
    }
}

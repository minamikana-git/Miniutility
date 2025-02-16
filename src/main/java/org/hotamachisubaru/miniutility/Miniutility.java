package org.hotamachisubaru.miniutility;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.Load;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.Chat;
import org.hotamachisubaru.miniutility.Listener.Utility;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.io.File;
import java.util.logging.Logger;

public class Miniutility extends JavaPlugin {
    private Chat chatListener;
    private NicknameManager nicknameManager;
    private final Logger logger = getLogger();
    private final PluginManager pm = getServer().getPluginManager();

    @Override
    public void onEnable() {
        chatListener = new Chat(this); // チャットリスナー
        nicknameManager = new NicknameManager(); // ニックネーム管理
        saveResource("nickname.db", false); // デフォルトリソースを保存
        saveDefaultConfig(); // config.ymlの作成
        checkLuckPerms(); // LuckPermsがあるかチェック
        registerListeners(); // イベントリスナー登録
        migration(); // データ移行処理を実行
        setupDatabase(); // データベースセットアップ
        registerCommands(); // コマンド登録
        logger.info("copyright 2024 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");
    }

    private void checkLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            logger.severe("LuckPermsが見つかりません。pluginsフォルダにあるか確認してください。");
            pm.disablePlugin(this);
        }
    }

    private void migration() {
        try {
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
                logger.warning("nickname.ymlのバックアップに失敗しました。");
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
        pm.registerEvents(new Utility(), this);
        pm.registerEvents(nicknameManager, this);
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public Chat getChatListener() {
        return chatListener;
    }
}

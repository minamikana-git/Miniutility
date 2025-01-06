package org.hotamachisubaru.miniutility;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.*;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.*;


import java.io.File;
import java.util.logging.Logger;

public class Miniutility extends JavaPlugin {
    private ChatListener chatListener;
    private NicknameManager nicknameManager;
    private Logger logger = getLogger();

    @Override
    public void onEnable() {
        chatListener = new ChatListener(this); // チャットリスナー
        nicknameManager = new NicknameManager(); // ニックネーム管理
        saveResource("nickname.db", false);// デフォルトリソースを保存
        saveDefaultConfig(); //config.ymlの作成
        checkLuckPerms(); //LuckPermsがあるかチェック
        registerListeners(); // イベントリスナー登録
        migration(); // データ移行処理を実行
        setupDatabase(); // データベースセットアップ
        registerCommands(); // コマンド登録
        logger.info("copyright 2024 hotamachisubaru all rights reserved.");
        logger.info("developed by hotamachisubaru");

    }

    private void checkLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null){
            logger.severe("LuckPermsが見つかりません。pluginsフォルダにあるか確認してください。");
            getServer().getPluginManager().disablePlugin(this);
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
                    database.saveNickname(key, nickname); // データベースに保存
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
            logger.info("データベースの設定が完了しました.");
        } catch (Exception e) {
            logger.severe("データベースの設定ができませんでした: " + e.getMessage());
        }
    }

    public void updateConfig(String key, Object value) {
        getConfig().set(key, value);
        saveConfig();
    }

    private void registerCommands() {
        getCommand("nick").setExecutor(new NicknameCommand(this));
        getCommand("menu").setExecutor(new UtilityCommand());
        getCommand("load").setExecutor(new Load());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(new UtilityListener(), this);
        Bukkit.getPluginManager().registerEvents(nicknameManager, this);
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }
}

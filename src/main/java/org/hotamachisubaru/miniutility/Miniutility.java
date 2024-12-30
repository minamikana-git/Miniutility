package org.hotamachisubaru.miniutility;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.*;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.*;


import java.io.File;
public class Miniutility extends JavaPlugin {
    private ChatListener chatListener;
    private NicknameManager nicknameManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        migration(); // データ移行処理を実行
        setupDatabase(); // データベースセットアップ
        chatListener = new ChatListener(this); // チャットリスナー
        nicknameManager = new NicknameManager(); // ニックネーム管理
        saveResource("nickname.db", false);
        checkLuckPerms();// デフォルトリソースを保存
        registerListeners(); // イベントリスナー登録
        registerCommands(); // コマンド登録
        getLogger().info("copyright 2024 hotamachisubaru all rights reserved.");
        getLogger().info("developed by hotamachisubaru");
    }

    private void checkLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null){
            getServer().getLogger().severe("LuckPermsが見つかりません。pluginsフォルダにあるか確認してください。");
            getServer().getPluginManager().disablePlugin(this);
        }
    }


    private void migration() {
        try {
            File oldFile = new File(getDataFolder(), "nickname.yml");
            if (!oldFile.exists()) {
                getLogger().info("統合するデータがありません。");
                return;
            }

            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            NicknameDatabase database = new NicknameDatabase(getDataFolder().getAbsolutePath());

            for (String key : oldConfig.getKeys(false)) {
                String nickname = oldConfig.getString(key);
                if (nickname != null) {
                    database.saveNickname(key, nickname); // データベースに保存
                    getLogger().info("ニックネームが移行されました：" + key);
                }
            }

            if (oldFile.renameTo(new File(getDataFolder(), "nickname.yml.bak"))) {
                getLogger().info("古いnickname.ymlは、nickname.yml.bakとしてバックアップされました。");
            } else {
                getLogger().warning("nickname.ymlのバックアップに失敗しました。");
            }
        } catch (Exception e) {
            getLogger().severe("ニックネームの統合に失敗しました: " + e.getMessage());
        }
    }




    private void setupDatabase() {
        try {
            NicknameDatabase database = new NicknameDatabase(getDataFolder().getAbsolutePath());
            database.setupDatabase();
            getLogger().info("データベースの設定が完了しました.");
        } catch (Exception e) {
            getLogger().severe("データベースの設定ができませんでした: " + e.getMessage());
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

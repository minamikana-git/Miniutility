package org.hotamachisubaru.miniutility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.*;


public class Miniutility extends JavaPlugin {
    private NicknameConfig nicknameConfig = null;
    private ChatListener chatListener = new ChatListener(this);
    private NicknameManager nicknameManager;
    @Override
    public void onEnable() {

        // Register event listeners
        registerListeners();
        // Commands
        registerCommands();

        // register nickname
        nicknameConfig = new NicknameConfig(this);
        nicknameManager = new NicknameManager(this);
        // Log startup information
        getLogger().info("copyright 2024 hotamachisubaru all rights reserved.");
        getLogger().info("developed by hotamachisubaru");

    }

    private void registerCommands() {
        getCommand("nick").setExecutor(new NicknameCommand(this));
        getCommand("menu").setExecutor(new UtilityCommand());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new UtilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new NicknameManager(this), this);
    }

    //ニックネームの読み込み
    @EventHandler
    public void loadNickname(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameConfig.getNickname(player.getUniqueId(), player.getName());
        if (nickname != null) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
        }
    }

    //ニックネームのセット
    public String setNickname(Player player, String nickname) {
        // バリデーション: 空白や長さの制限を設定
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("無効なニックネームです。空白にすることはできません。");
        }
        if (nickname.length() > 16) {
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");
        }

        // ニックネームを保存
        nicknameConfig.setNickname(player.getUniqueId(), nickname);
        saveConfig();

        // プレイヤーの表示名とリスト名を更新
        player.setDisplayName(nickname);
        player.setPlayerListName(nickname);

        return nickname;
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public NicknameConfig getNicknameConfig() {
        return nicknameConfig;
    }
    public ChatListener getChatListener() {
        return chatListener;
    }

}

package org.hotamachisubaru.miniutility;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.*;

public class Miniutility extends JavaPlugin {
    private NicknameConfig nicknameConfig;
    private ChatListener chatListener;
    private NicknameManager nicknameManager;

    @Override
    public void onEnable() {
        nicknameConfig = new NicknameConfig(this);
        chatListener = new ChatListener(this);
        nicknameManager = new NicknameManager(nicknameConfig); // NicknameConfigを渡す

        // Register event listeners
        registerListeners();
        // Commands
        registerCommands();

        // Log startup information
        getLogger().info("copyright 2024 hotamachisubaru all rights reserved.");
        getLogger().info("developed by hotamachisubaru");
    }

    private void registerCommands() {
        getCommand("nick").setExecutor(new NicknameCommand(this));
        getCommand("menu").setExecutor(new UtilityCommand());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(new UtilityListener(), this);
        Bukkit.getPluginManager().registerEvents(nicknameManager, this); // 修正済み
    }


    // プレイヤーがログインした際にニックネームを適用
    @EventHandler
    public void loadNickname(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameConfig.getNickname(player.getUniqueId(), player.getName());
        if (nickname != null && !nickname.trim().isEmpty()) {
            applyFormattedNickname(player, nickname);
        }
    }

    // ニックネームのセット
    public String setNickname(Player player, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("無効なニックネームです。空白にすることはできません。");
        }
        if (nickname.length() > 16) {
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");
        }

        nicknameConfig.setNickname(player.getUniqueId(), nickname);
        return applyFormattedNickname(player, nickname);
    }

    // ニックネームとLuckPermsのプレフィックスを適用
    private String applyFormattedNickname(Player player, String nickname) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);

        String prefix = metaData.getPrefix();
        if (prefix == null) {
            prefix = ""; // プレフィックスが設定されていない場合は空にする
        }

        String formattedNickname = ChatColor.translateAlternateColorCodes('&', prefix + nickname);
        player.setDisplayName(formattedNickname);
        player.setPlayerListName(formattedNickname);

        player.sendMessage(ChatColor.GREEN + "ニックネームが設定されました: " + formattedNickname);
        return formattedNickname;
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

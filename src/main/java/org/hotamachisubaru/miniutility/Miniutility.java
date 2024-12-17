package org.hotamachisubaru.miniutility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.UtilityCommand;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.NicknameCommand;
import org.hotamachisubaru.miniutility.Nickname.NicknameConfig;

public class Miniutility extends JavaPlugin {
    private NicknameConfig nicknameConfig = null;
    private ChatListener chatListener = new ChatListener();

    @Override
    public void onEnable() {

        // Register event listeners
        registerListeners();
        // Commands
        registerCommands();
        // Initialize nickname config
        nicknameConfig = new NicknameConfig(this);
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
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = nicknameConfig.getNickname(player.getUniqueId(), player.getName());
        if (nickname != null) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
        }
    }

    public NicknameConfig getNicknameConfig() {
        return nicknameConfig;
    }
    public ChatListener getChatListener() {
        return chatListener;
    }

}

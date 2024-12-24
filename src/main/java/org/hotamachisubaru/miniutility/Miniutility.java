package org.hotamachisubaru.miniutility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.miniutility.Command.*;
import org.hotamachisubaru.miniutility.Listener.*;
import org.hotamachisubaru.miniutility.Nickname.*;

import java.sql.SQLException;


public class Miniutility extends JavaPlugin {
    private NicknameConfig nicknameConfig;
    private ChatListener chatListener;
    private NicknameManager nicknameManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupDatabase();
        nicknameConfig = new NicknameConfig();
        chatListener = new ChatListener(this);
        nicknameManager = new NicknameManager(nicknameConfig); // NicknameConfigを渡す
        saveResource("nickname.db",false);
        // Register event listeners
        registerListeners();
        // Commands
        registerCommands();
        // Log startup information
        getLogger().info("copyright 2024 hotamachisubaru all rights reserved.");
        getLogger().info("developed by hotamachisubaru");
    }

    private void setupDatabase() {
        String dbPath = getConfig().getString("database.path", "nickname.db");
        boolean autoCreate = getConfig().getBoolean("database.autoCreate", true);

        try {
            NicknameDatabase database = new NicknameDatabase("nickname.db");
            database.openConnection(getDataFolder().getAbsolutePath() + "/" + dbPath);

            if (autoCreate) {
                database.setupDatabase();
            }
            getLogger().info("Database setup completed.");
        } catch (SQLException e) {
            getLogger().severe("Failed to setup the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateConfig(String key, Object value) {
        getConfig().set(key, value);
        saveConfig();
    }



    private void registerCommands() {
        getCommand("nick").setExecutor(new NicknameCommand(this));
        getCommand("menu").setExecutor(new UtilityCommand());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(new UtilityListener(), this);
        Bukkit.getPluginManager().registerEvents(nicknameManager, this);
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

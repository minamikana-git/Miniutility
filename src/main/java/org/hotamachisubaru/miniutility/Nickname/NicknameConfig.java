package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NicknameConfig {
    private final FileConfiguration config;
    private final File configFile;

    public NicknameConfig(JavaPlugin plugin) {
        this.configFile = new File(plugin.getDataFolder(), "nickname.yml");
        if (!configFile.exists()) {
            plugin.saveResource("nickname.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getNickname(UUID uuid, String playerName) {
        return config.getString("nicknames." + uuid, playerName);
    }

    public void setNickname(UUID uuid, String nickname) {
        config.set("nicknames." + uuid, nickname);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException("設定ファイルの保存に失敗しました。", e);
        }
    }
}



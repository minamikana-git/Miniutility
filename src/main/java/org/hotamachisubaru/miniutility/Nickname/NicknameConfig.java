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

    public String getNickname(UUID uuid) {
        return config.getString("nicknames." + uuid.toString(), null);
    }

    public void setNickname(UUID uuid, String nickname) {
        config.set("nicknames." + uuid.toString(), nickname);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


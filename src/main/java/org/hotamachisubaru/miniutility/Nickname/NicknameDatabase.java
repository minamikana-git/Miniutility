package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.configuration.file.FileConfiguration;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameDatabase {
    private final MiniutilityLoader plugin;
    private final Map<String, String> nicknameMap = new ConcurrentHashMap<>();

    public NicknameDatabase(MiniutilityLoader plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        if (config.isConfigurationSection("nicknames")) {
            for (String uuid : config.getConfigurationSection("nicknames").getKeys(false)) {
                String nick = config.getString("nicknames." + uuid, "");
                if (!nick.isEmpty()) nicknameMap.put(uuid, nick);
            }
        }
    }

    public void saveAll() {
        FileConfiguration config = plugin.getConfig();
        config.set("nicknames", null);
        for (Map.Entry<String, String> entry : nicknameMap.entrySet()) {
            config.set("nicknames." + entry.getKey(), entry.getValue());
        }
        plugin.saveConfig();
    }

    public String getNickname(String uuid) {
        return nicknameMap.getOrDefault(uuid, "");
    }

    public void setNickname(String uuid, String nickname) {
        nicknameMap.put(uuid, nickname);
        saveAll();
    }

    public void removeNickname(String uuid) {
        nicknameMap.remove(uuid);
        saveAll();
    }

    public void reload() {
        nicknameMap.clear();
        load();
    }
}

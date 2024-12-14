package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Miniutility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NicknameConfig {
    private final Miniutility plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Boolean> waitingForNicknameInput = new HashMap<>();

    public NicknameConfig(Miniutility plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "nickname.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("nickname.ymlを作成できませんでした。");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void setNickname(UUID uuid, String nickname) {
        config.set(uuid.toString(), nickname);
        save();
    }

    public String getNickname(UUID uuid, String defaultName) {
        return config.getString(uuid.toString(), defaultName);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("nickname.ymlの保存に失敗しました。");
        }
    }

    public void setWaitingForNickname(Player player, boolean b) {
        waitingForNicknameInput.put(player.getUniqueId(), b);
    }

    public boolean isWaitingForNickname(Player player) {
        return waitingForNicknameInput.getOrDefault(player.getUniqueId(), false);
    }
}

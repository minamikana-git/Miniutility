package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NicknameConfig {
    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;

    public NicknameConfig(JavaPlugin plugin) {
        this.plugin = plugin;

        // ディレクトリが存在しない場合は作成
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // nickname.ymlファイルを作成またはロード
        file = new File(plugin.getDataFolder(), "nickname.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    // ニックネームを設定
    public void setNickname(UUID uuid, String nickname) {
        config.set("nicknames." + uuid.toString(), nickname);
        saveConfig(); // 保存処理
    }

    // ニックネームを取得
    public String getNickname(UUID uuid, String defaultName) {
        return config.getString("nicknames." + uuid.toString(), defaultName);
    }

    // 設定を保存
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("設定ファイルの保存に失敗しました。");
        }
    }
}

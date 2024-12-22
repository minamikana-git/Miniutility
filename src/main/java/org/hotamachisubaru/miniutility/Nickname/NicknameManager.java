package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NicknameManager implements Listener {
    private final File nicknameFile;
    private final FileConfiguration nicknameConfig;

    public NicknameManager(JavaPlugin plugin) {
        // ファイルの読み込み
        nicknameFile = new File(plugin.getDataFolder(), "nickname.yml");
        if (!nicknameFile.exists()) {
            try {
                nicknameFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe(ChatColor.RED + "nickname.ymlを作成できませんでした");
            }
        }
        nicknameConfig = YamlConfiguration.loadConfiguration(nicknameFile);
    }

    // プレイヤーがログインした際にニックネームを適用
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String nickname = nicknameConfig.getString(playerUUID.toString());

        if (nickname != null && !nickname.isEmpty()) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
            player.sendMessage(ChatColor.GREEN + "ようこそ、" + nickname + " さん！");
        } else {
            player.sendMessage(ChatColor.YELLOW + "ニックネームが設定されていません。");
        }
    }


    // ニックネームを設定して保存
    public String setNickname(Player player, String nickname) {
        nicknameConfig.set(player.getUniqueId().toString(), nickname);
        saveConfig();
        player.setDisplayName(nickname);
        player.setPlayerListName(nickname);
        return nickname;
    }

    // 設定を保存
    private void saveConfig() {
        try {
            nicknameConfig.save(nicknameFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(ChatColor.RED + "nickname.ymlを保存できませんでした");
        }
    }
}


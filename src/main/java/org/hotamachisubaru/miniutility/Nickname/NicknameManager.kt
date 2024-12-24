package org.hotamachisubaru.miniutility.Nickname;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class NicknameManager implements Listener {
    private final NicknameConfig nicknameConfig;

    public NicknameManager(NicknameConfig nicknameConfig) {
        this.nicknameConfig = nicknameConfig;
    }

    public String setNicknameWithPrefix(Player player, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            String defaultName = player.getName();
            nicknameConfig.setNickname(player.getUniqueId(), null);

            player.setDisplayName(defaultName);
            player.setPlayerListName(defaultName);

            player.sendMessage(ChatColor.YELLOW + "ニックネームをリセットしました。");
            return defaultName;
        }

        if (!nickname.contains("&")) {
            nickname = "&f" + nickname;
        }

        if (!nickname.matches(".*&[0-9a-fA-F].*")) {
            throw new IllegalArgumentException("無効なカラーコードです。例：&6ニックネーム");
        }

        String formattedNickname = ChatColor.translateAlternateColorCodes('&', nickname);

        // LuckPermsからプレフィックスを取得
        LuckPerms luckPerms = LuckPermsProvider.get();
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String prefix = metaData.getPrefix();

        if (prefix == null) {
            prefix = ""; // プレフィックスがない場合は空にする
        }

        String displayName = ChatColor.translateAlternateColorCodes('&', prefix) + formattedNickname;

        nicknameConfig.setNickname(player.getUniqueId(), formattedNickname);

        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);

        player.sendMessage(ChatColor.GREEN + "ニックネームが設定されました。" + displayName);

        return displayName;
    }
}

package org.hotamachisubaru.miniutility.Nickname;


import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class NicknameManager implements Listener {

    private static final Logger logger = Logger.getLogger("Miniutility");

    @EventHandler
    public void loadNickname(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        logger.info("Player joined: " + player.getName());
        applyFormattedDisplayName(player);
    }

    public static String setNickname(Player player, String nickname) throws SQLException {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("無効なニックネームです。空白にすることはできません。");
        }
        if (nickname.length() > 16) {
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");
        }

        NicknameManager.setNickname(player, nickname);
        logger.info("Setting nickname for player " + player.getName() + ": " + nickname);

        applyFormattedDisplayName(player);
        player.sendMessage(ChatColor.GREEN + "ニックネームが設定されました: " + nickname);
        return nickname;
    }

    public static void applyFormattedDisplayName(Player player) {
        String prefix = getLuckPermsPrefix(player);
        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
        }

        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + nickname);
        player.setDisplayName(formattedName);
        player.setPlayerListName(formattedName);
    }

    private static String getLuckPermsPrefix(Player player) {
        CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
        return metaData.getPrefix() != null ? metaData.getPrefix() : "";
    }

    private String translateHexColorCodes(String message) {
        Pattern hexPattern = Pattern.compile("(?i)&#([0-9a-fA-F]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String replacement = matcher.group(1).chars()
                    .mapToObj(c -> "§" + (char) c)
                    .reduce("§x", String::concat);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }


    @EventHandler
    public void applyNickname(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setFormat(player.getDisplayName() + ": " + event.getMessage());
    }
}

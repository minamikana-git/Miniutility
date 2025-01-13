package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
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

        applyFormattedDisplayName(player);
        player.sendMessage(Component.text(ChatColor.GREEN + "ニックネームが設定されました: " + nickname));
        return nickname;
    }

    public static void applyFormattedDisplayName(Player player) {
        String prefix = getLuckPermsPrefix(player);
        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
        }

        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + nickname);
        player.displayName(Component.text(formattedName));
        player.playerListName(Component.text(formattedName));
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
    public void applyNickname(AsyncChatEvent event) {
        Player player = event.getPlayer();
        event.renderer((sender, displayName, message, viewers) ->
                Component.text(player.getDisplayName() + ": ").append(message));
    }
}

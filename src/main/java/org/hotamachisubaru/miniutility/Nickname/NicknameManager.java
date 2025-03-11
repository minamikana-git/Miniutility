package org.hotamachisubaru.miniutility.Nickname;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NicknameManager implements Listener {

    private static final Logger logger = Logger.getLogger("Miniutility");

    private static final Map<Player, Boolean> waitingForNickname = new HashMap<>();

    public static void setWaitingForNickname(Player player, boolean b) {
        if (b) {
            waitingForNickname.put(player, true);
        } else {
            waitingForNickname.remove(player);
        }
    }

    @EventHandler
    public void loadNickname(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyFormattedDisplayName(player);
    }

    public static void setNickname(Player player, String nickname) throws SQLException {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("無効なニックネームです。空白にすることはできません。");
        }
        if (nickname.length() > 16) {
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");
        }

        NicknameDatabase.saveNickname(player.getUniqueId().toString(), nickname);
        applyFormattedDisplayName(player);
        player.sendMessage(Component.text("ニックネームが設定されました: " + nickname, NamedTextColor.GREEN));
    }

    public static void applyFormattedDisplayName(Player player) {
        String prefix = getLuckPermsPrefix(player);
        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
        }

        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + nickname);
        player.displayName(LegacyComponentSerializer.legacy('&').deserialize(formattedName));
        player.playerListName(LegacyComponentSerializer.legacy('&').deserialize(formattedName));
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
                Component.text(player.displayName() + ": ").append(message));
    }
}


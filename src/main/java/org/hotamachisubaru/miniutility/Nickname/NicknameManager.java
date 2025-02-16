package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NicknameManager implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyFormattedDisplayName(player);
    }

    public static void applyFormattedDisplayName(Player player) {
        Component displayComponent = getDisplayComponent(player);
        player.displayName(displayComponent);
        player.playerListName(displayComponent);
    }

    public static Component getDisplayComponent(Player player) {
        String prefix = getLuckPermsPrefix(player);
        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
        }

        nickname = translateHexColorCodes(nickname);
        String combined = prefix + nickname;
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(combined);

        return component;
    }

    private static String translateHexColorCodes(String message) {
        Pattern pattern = Pattern.compile("(?i)&#([0-9A-F]{6})");
        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder legacyCode = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                legacyCode.append("§").append(c);
            }
            matcher.appendReplacement(sb, legacyCode.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String getLuckPermsPrefix(Player player) {
        CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
        return metaData.getPrefix() != null ? metaData.getPrefix() : "";
    }
}

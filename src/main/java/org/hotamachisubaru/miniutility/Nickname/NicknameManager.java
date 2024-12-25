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
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NicknameManager implements Listener {

    private final NicknameConfig nicknameConfig;
    private final Logger logger = Logger.getLogger("Miniutility");

    public NicknameManager(NicknameConfig nicknameConfig) {
        this.nicknameConfig = nicknameConfig;
    }

    @EventHandler
    public void loadNickname(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        logger.info("Player joined: " + player.getName());
        applyFormattedDisplayName(player);
    }

    public String setNickname(Player player, String nickname) throws SQLException {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("無効なニックネームです。空白にすることはできません。");
        }
        if (nickname.length() > 16) {
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");
        }

        nicknameConfig.setNickname(player.getUniqueId(), nickname);
        logger.info("Setting nickname for player " + player.getName() + ": " + nickname);

        applyFormattedDisplayName(player);
        player.sendMessage(ChatColor.GREEN + "ニックネームが設定されました: " + nickname);
        return nickname;
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

    public void applyFormattedDisplayName(Player player) {
        CachedMetaData metaData = LuckPermsProvider.get()
                .getPlayerAdapter(Player.class)
                .getMetaData(player);

        // LuckPermsのPrefixを取得
        String prefix = metaData.getPrefix();
        if (prefix == null) {
            prefix = ""; // プレフィックスがnullの場合は空文字
        }

        // データベースからニックネームを取得
        String nickname = NicknameDatabase.loadNicknameFromDatabase(player.getUniqueId().toString());
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = player.getName(); // ニックネームがない場合はプレイヤー名
        }

        // Prefixとニックネームを結合
        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + " " + nickname);

        // DisplayNameとPlayerListNameを設定
        player.setDisplayName(formattedName);
        player.setPlayerListName(formattedName);

        logger.info("Formatted display name for player " + player.getName() + ": " + formattedName);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setFormat(player.getDisplayName() + ": " + event.getMessage());
    }
}

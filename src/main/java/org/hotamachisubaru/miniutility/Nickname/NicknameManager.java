package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyFormattedDisplayName(player);
    }

    /**
     * ニックネームの設定またはリセットを行い、表示名を更新します。
     * @param player 対象プレイヤー
     * @param nickname 入力されたニックネーム（空文字の場合はリセット）
     * @return 保存されたニックネーム
     * @throws SQLException
     */
    public static String setNickname(Player player, String nickname) throws SQLException {
        if (nickname.trim().isEmpty()) {
            NicknameDatabase.saveNickname(player.getUniqueId().toString(), "");
            applyFormattedDisplayName(player);
            player.sendMessage(Component.text(ChatColor.GREEN + "ニックネームをリセットしました。"));
            return "";
        }
        if (nickname.length() > 16) {
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");
        }
        NicknameDatabase.saveNickname(player.getUniqueId().toString(), nickname);
        applyFormattedDisplayName(player);
        player.sendMessage(Component.text(ChatColor.GREEN + "ニックネームが設定されました: " + nickname));
        return nickname;
    }

    /**
     * プレイヤーの表示名とタブリスト名を更新します。
     * LuckPerms のプレフィックス、DBに保存されたニックネーム（またはプレイヤー名）、
     * 入力された hex カラーコード（&#RRGGBB）および &カラーコードを反映させます。
     * Adventure API の LegacyComponentSerializer で Component に変換します。
     * @param player 対象プレイヤー
     */
    public static void applyFormattedDisplayName(Player player) {
        Component displayComponent = getDisplayComponent(player);
        player.displayName(displayComponent);
        player.playerListName(displayComponent);
    }

    /**
     * プレイヤーの表示名の Component を生成します。
     * @param player 対象プレイヤー
     * @return Component 化された表示名
     */
    public static Component getDisplayComponent(Player player) {
        String prefix = getLuckPermsPrefix(player);
        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
        }
        // まず、hexカラーコード（例: "&#6xxxxxx"）を legacy な §x形式に変換
        nickname = translateHexColorCodes(nickname);
        // その後、&記号によるカラーコードを置換
        String combined = ChatColor.translateAlternateColorCodes('&', prefix + nickname);
        // legacy形式の文字列を Component に変換
        return LegacyComponentSerializer.legacySection().deserialize(combined);
    }

    /**
     * 文字列中の hex カラーコード（例: "&#RRGGBB"）を legacy な §x 形式に変換します。
     * 例: "&#6F6F6F" → "§x§6§F§6§F§6§F"
     * @param message 入力文字列
     * @return 変換後の文字列
     */
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

    /**
     * LuckPerms のプレフィックスを取得します。
     * @param player 対象プレイヤー
     * @return プレフィックス（なければ空文字）
     */
    private static String getLuckPermsPrefix(Player player) {
        CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
        return metaData.getPrefix() != null ? metaData.getPrefix() : "";
    }

    /**
     * チャットイベント時に、プレイヤーの表示名を先頭に付与してチャットをフォーマットします。
     * ここでも、getDisplayComponent() で生成した Component を legacy 文字列に変換して利用します。
     * ※ ※ プレイヤーの displayName は Component として設定しているため、renderer 内で
     * LegacyComponentSerializer を用いて文字列化しています。
     */
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Component displayComponent = getDisplayComponent(player);
        // legacy 文字列に変換してから、":" を付与
        String prefix = LegacyComponentSerializer.legacySection().serialize(displayComponent);
        event.setFormat(prefix + ": %2$s");
    }
}

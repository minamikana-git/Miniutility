package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.logging.Logger;

public class NicknameManager implements Listener {

    private static final Logger logger = Logger.getLogger("Miniutility");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacy('&');

    public static String getLuckPermsPrefix(Player player) {
        try {
            var metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
            return metaData.getPrefix() != null ? metaData.getPrefix() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /** プレイヤーがサーバーにjoin時に、ニックネーム＋色を反映 */
    @EventHandler
    public void loadNickname(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyFormattedDisplayName(player);
    }

    /** ニックネーム（色コード付き）をデータベースに保存し、即時反映 */
    public static void setNickname(Player player, String nickname) throws SQLException {
        if (nickname.trim().isEmpty())
            throw new IllegalArgumentException("無効なニックネームです。空白にすることはできません。");
        if (nickname.length() > 16)
            throw new IllegalArgumentException("ニックネームは16文字以内にしてください。");

        NicknameDatabase.saveNickname(player.getUniqueId().toString(), nickname);
        applyFormattedDisplayName(player);
        player.sendMessage(Component.text("ニックネームが設定されました: " + nickname, NamedTextColor.GREEN));
    }

    /** プレイヤーの表示名（TABリスト・displayName）を色付き・プレフィックス込みで反映 */
    public static void applyFormattedDisplayName(Player player) {
        String uuid = player.getUniqueId().toString();
        String storedNick = NicknameDatabase.getNickname(uuid);
        if (storedNick == null || storedNick.isBlank())
            storedNick = player.getName();

        // プレフィックス（LuckPerms）取得
        String prefix = "";
        try {
            CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
            prefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";
        } catch (Exception e) {
            logger.warning("LuckPerms未ロード: " + player.getName());
        }

        // プレフィックス＋色付きニックネームでComponent化
        Component comp = Component.empty()
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix))
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(storedNick));

        player.displayName(comp);
        player.playerListName(comp);
    }

    /** データベースからニックネームを取得（nullならプレイヤー名） */
    public static String getNickname(Player player) {
        String nick = NicknameDatabase.getNickname(player.getUniqueId().toString());
        return (nick == null || nick.isBlank()) ? player.getName() : nick;
    }
}

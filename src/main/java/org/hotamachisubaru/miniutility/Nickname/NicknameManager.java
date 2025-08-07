package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ニックネーム管理（バージョン分岐例付き）
 */
public class NicknameManager {
    private static final Logger logger = Bukkit.getLogger();
    private static final Map<UUID, String> nicknameMap = new ConcurrentHashMap<>();

    public static void setNickname(Player player, String nickname) {
        nicknameMap.put(player.getUniqueId(), nickname);
        updateDisplayName(player);
    }

    public static void removeNickname(Player player) {
        nicknameMap.remove(player.getUniqueId());
        updateDisplayName(player);
    }

    public static String getDisplayName(Player player) {
        String nickname = nicknameMap.get(player.getUniqueId());
        return (nickname != null) ? nickname : player.getName();
    }

    public static void updateDisplayName(Player player) {
        String nickname = getDisplayName(player);

        // Prefix取得（LuckPermsなど）
        String prefix = "";
        try {
            var meta = net.luckperms.api.LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
            prefix = meta.getPrefix() == null ? "" : meta.getPrefix();
        } catch (Throwable ignored) {}

        // 新APIならAdventure Componentで、旧APIならsetDisplayName
        String displayName = prefix + nickname;

        try {
            if (APIVersionUtil.isAtLeast(19)) {
                // Adventure APIでカラーコード反映
                Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
                player.displayName(comp);
            } else {
                // 旧API: そのままsetDisplayName
                player.setDisplayName(displayName.replace('&', '§'));
            }
        } catch (Throwable e) {
            // どちらも失敗した場合の保険
            player.setDisplayName(displayName.replace('&', '§'));
            logger.warning("表示名の設定に失敗しました: " + e.getMessage());
        }
    }
}

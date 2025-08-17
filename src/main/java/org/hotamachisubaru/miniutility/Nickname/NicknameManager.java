package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ニックネーム管理（バージョン分岐例付き）
 */
public class NicknameManager {
    private final Map<UUID, Boolean> prefixEnabled = new HashMap<>();
    private static final Logger logger = Bukkit.getLogger();
    public static final Map<UUID, String> nicknameMap = new ConcurrentHashMap<>();
    private static NicknameDatabase nicknameDatabase = new NicknameDatabase();

    public NicknameManager(NicknameDatabase nicknameDatabase) {
        NicknameManager.nicknameDatabase = nicknameDatabase;
    }
    public static void init() {
        NicknameDatabase.init();
        NicknameDatabase.reload();
    }
    public static void setNickname(Player player, String nickname) {
        // DB保存
        nicknameDatabase.setNickname(player.getUniqueId().toString(), nickname);
        // メモリキャッシュ
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
            CachedMetaData meta = net.luckperms.api.LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
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

    public boolean togglePrefix(@NotNull UUID uniqueId) {
        boolean current = prefixEnabled.getOrDefault(uniqueId, false);
        boolean newState = !current;
        prefixEnabled.put(uniqueId, newState);
        return newState;
    }


    public static boolean setColor(Player player, ChatColor color) {
        if (player == null || color == null || !color.isColor()) return false;
        UUID uuid = player.getUniqueId();
        String nick = nicknameMap.get(uuid);
        if (nick == null || nick.isEmpty()) return false;
        String base = stripLeadingLegacyCodes(nick);
        nicknameMap.put(uuid, color + base);
        updateDisplayName(player);
        return true;
    }
    private static String stripLeadingLegacyCodes(String s){
        if (s == null) return null;
        int i=0; while (i+1 < s.length()){
            char c0=s.charAt(i), c1=Character.toLowerCase(s.charAt(i+1));
            boolean mark=(c0=='§'||c0=='&');
            boolean code=(c1>='0'&&c1<='9')||(c1>='a'&&c1<='f')||
                    (c1=='k'||c1=='l'||c1=='m'||c1=='n'||c1=='o'||c1=='r');
            if (mark && code) i+=2; else break;
        }
        return s.substring(i);
    }


}

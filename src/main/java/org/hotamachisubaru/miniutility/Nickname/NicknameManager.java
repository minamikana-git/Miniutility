package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ニックネーム管理（Paper 1.19+ の Adventure 新API と 旧API の両対応）
 */
public class NicknameManager {

    // --- state ---
    private static final Logger logger = Bukkit.getLogger();

    /** uuid -> nickname (&/§ レガシーコード可) */
    public static final Map<UUID, String> nicknameMap = new ConcurrentHashMap<>();

    /** uuid -> prefix 表示のON/OFF */
    private static final Map<UUID, Boolean> prefixEnabled = new ConcurrentHashMap<>();

    /** DB アクセス */
    private static NicknameDatabase nicknameDatabase = new NicknameDatabase();

    public NicknameManager(NicknameDatabase nicknameDatabase) {
        NicknameManager.nicknameDatabase = nicknameDatabase;
    }

    // ------------------------------------------------------------
    // public API
    // ------------------------------------------------------------

    public static void init() {
        NicknameDatabase.init();
        NicknameDatabase.reload();
    }

    public static void setNickname(Player player, String nickname) {
        if (player == null) return;
        // DB保存
        nicknameDatabase.setNickname(player.getUniqueId().toString(), nickname);
        // キャッシュ
        nicknameMap.put(player.getUniqueId(), nickname);
        // 表示更新
        updateDisplayName(player);
    }

    public static void removeNickname(Player player) {
        if (player == null) return;
        nicknameMap.remove(player.getUniqueId());
        updateDisplayName(player);
    }

    public static String getDisplayName(Player player) {
        if (player == null) return "";
        String nickname = nicknameMap.get(player.getUniqueId());
        return (nickname != null) ? nickname : player.getName();
    }

    /**
     * Prefix の表示をトグル。戻り値は新しい状態（true=表示）
     */
    public boolean togglePrefix(@NotNull UUID uniqueId) {
        boolean newState = !prefixEnabled.getOrDefault(uniqueId, true);
        prefixEnabled.put(uniqueId, newState);
        // その場で表示を更新したい場合は呼び出し側で updateDisplayName を呼ぶ
        return newState;
    }

    /**
     * 先頭の色/装飾コードを外して、新しい色で付け直し
     */
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

    /**
     * 表示名（チャット名／タブ名）を更新
     * Paper 1.19+ : Adventure コンポーネント
     * 旧API       : setDisplayName / setPlayerListName
     */
    public static void updateDisplayName(Player player) {
        if (player == null) return;

        // --- Nickname + (必要なら) Prefix を合成 ---
        String nickname = getDisplayName(player);

        String prefix = "";
        try {
            // Prefix はデフォルト表示ON。togglePrefix(false)なら空にする。
            boolean show = prefixEnabled.getOrDefault(player.getUniqueId(), true);

            if (show) {
                CachedMetaData meta = net.luckperms.api.LuckPermsProvider.get()
                        .getPlayerAdapter(Player.class).getMetaData(player);
                prefix = (meta.getPrefix() == null) ? "" : meta.getPrefix();
            }
        } catch (Throwable ignored) { /* LuckPerms 無しでもOK */ }

        String legacy = prefix + nickname; // &/§ を含む可能性あり

        // --- 新旧API両対応で反映 ---
        if (!applyAdventureIfAvailable(player, legacy)) {
            // 旧APIへフォールバック
            String colored = translateAltColorCodes(legacy);
            try {
                player.setDisplayName(colored);
            } catch (Throwable ignored) {}
            try {
                player.setPlayerListName(colored);
            } catch (Throwable ignored) {}
        }
    }

    // ------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------

    /** 先頭のレガシーコード（色/装飾）を取り除く */
    private static String stripLeadingLegacyCodes(String s){
        if (s == null) return null;
        int i = 0;
        while (i + 1 < s.length()) {
            char c0 = s.charAt(i);
            char c1 = Character.toLowerCase(s.charAt(i + 1));
            boolean mark = (c0 == '§' || c0 == '&');
            boolean code =
                    (c1 >= '0' && c1 <= '9') ||
                            (c1 >= 'a' && c1 <= 'f') ||
                            (c1 == 'k' || c1 == 'l' || c1 == 'm' || c1 == 'n' || c1 == 'o' || c1 == 'r');
            if (mark && code) i += 2;
            else break;
        }
        return s.substring(i);
    }

    /** & -> § 変換（旧API用） */
    private static String translateAltColorCodes(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Paper 1.19+ の Adventure API が使えるなら、displayName / playerListName に Component を適用。
     * 利用できなければ false を返す。
     */
    private static boolean applyAdventureIfAvailable(Player player, String legacyText) {
        try {
            // 1) &/§ を両方受けるため、§ を & に寄せてからパース
            String normalized = legacyText == null ? "" : legacyText.replace('§', '&');
            Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(normalized);

            // 2) Player#displayName(Component)
            Method mDisplay = Player.class.getMethod("displayName", Component.class);
            mDisplay.invoke(player, comp);

            // 3) Player#playerListName(Component) があればタブ名も更新
            try {
                Method mList = Player.class.getMethod("playerListName", Component.class);
                mList.invoke(player, comp);
            } catch (NoSuchMethodException ignore) {
                // 古いPaper：String APIにフォールバック
                try {
                    player.setPlayerListName(translateAltColorCodes(legacyText));
                } catch (Throwable ignored2) {}
            }
            return true;
        } catch (NoSuchMethodException e) {
            // Adventure のメソッドが無い（=旧API）
            return false;
        } catch (Throwable t) {
            logger.warning("[Miniutility] Failed to apply Adventure display name: " + t.getMessage());
            return false;
        }
    }
}

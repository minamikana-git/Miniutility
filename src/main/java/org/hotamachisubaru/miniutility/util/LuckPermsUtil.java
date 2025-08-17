package org.hotamachisubaru.miniutility.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;

public final class LuckPermsUtil {

    private LuckPermsUtil() {} // インスタンス化禁止

    public static String safePrefix(Player player) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            CachedMetaData meta = api.getPlayerAdapter(Player.class).getMetaData(player);
            return meta.getPrefix() == null ? "" : meta.getPrefix();
        } catch (IllegalStateException | NoClassDefFoundError e) {
            // LuckPerms が無い / まだロードされていない時
            return "";
        }
    }
}


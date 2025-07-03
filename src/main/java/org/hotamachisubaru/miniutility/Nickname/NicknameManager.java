package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Miniutility;
import org.hotamachisubaru.miniutility.util.FoliaUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameManager {
    private final Miniutility plugin;
    private final NicknameDatabase db;
    private final Map<UUID,Boolean> prefixEnabledMap = new ConcurrentHashMap<>();

    public NicknameManager(Miniutility plugin, NicknameDatabase db) {
        this.plugin = plugin;
        this.db = db;
    }

    public String getNickname(Player player) {
        return db.getNickname(player.getUniqueId().toString());
    }

    public void setNickname(Player player, String nickname) {
        db.setNickname(player.getUniqueId().toString(), nickname);
        applyFormattedDisplayName(player);
    }

    public void clearNickname(Player player) {
        db.removeNickname(player.getUniqueId().toString());
        applyFormattedDisplayName(player);
    }

    public static String getLuckPermsPrefix(Player player) {
        try {
            net.luckperms.api.cacheddata.CachedMetaData metaData = net.luckperms.api.LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
            return metaData.getPrefix() == null ? "" : metaData.getPrefix();
        } catch (Exception e) {
            return "";
        }
    }

    public void applyFormattedDisplayName(Player player) {
        boolean showPrefix = prefixEnabledMap.getOrDefault(player.getUniqueId(),true);
        String prefix = showPrefix ? getLuckPermsPrefix(player) : "";
        String nickname = getNickname(player);
        String display = "";
        if (prefix != null && !prefix.isEmpty()) display += prefix;
        if (nickname != null && !nickname.isEmpty()) display += nickname;
        else display += player.getName();

        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(display);

        FoliaUtil.runAtPlayer(plugin, player, () -> {
            player.displayName(comp);
            player.playerListName(comp);
        });
    }

    public boolean togglePrefix(@NotNull UUID uniqueId) {
        boolean current = prefixEnabledMap.getOrDefault(uniqueId,true);
        prefixEnabledMap.put(uniqueId,!current);
        return !current;
    }
}



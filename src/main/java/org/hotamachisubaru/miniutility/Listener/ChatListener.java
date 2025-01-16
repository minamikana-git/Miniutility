package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.plugin.Plugin;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {
    private final Plugin plugin;
    private static final Map<UUID, Boolean> waitingForNickname = new HashMap<>();
    private static final Map<UUID, Boolean> waitingForColorInput = new HashMap<>();

    public ChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void setWaitingForNickname(Player player, boolean waiting) {
        waitingForNickname.put(player.getUniqueId(), waiting);
    }

    public static void setWaitingForColorInput(Player player, boolean waiting) {
        waitingForColorInput.put(player.getUniqueId(), waiting);
    }

    public boolean isWaitingForNickname(Player player) {
        return waitingForNickname.getOrDefault(player.getUniqueId(), false);
    }

    public boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.getOrDefault(player.getUniqueId(), false);
    }

    @EventHandler
    public void Nickname(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            NicknameInput(player, event.message());
        } else if (waitingForColorInput.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            ColorInput(player, event.message());
        }
    }

    private void NicknameInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            if (!message.isEmpty()) {
                // ニックネームをデータベースに保存
                NicknameDatabase.saveNickname(player.getUniqueId().toString(), message);

                // 表示名を更新
                updateDisplayNamePrefix(player, message);

                player.sendMessage(Component.text(ChatColor.GREEN + "ニックネームを設定しました: " + message));
            } else {
                player.sendMessage(Component.text(ChatColor.RED + "無効なニックネームです。"));
            }
            waitingForNickname.put(player.getUniqueId(), false);
        });
    }

    public void ColorInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        if (message.isEmpty()) {
            player.sendMessage(Component.text(ChatColor.RED + "無効な入力です。カラーコードを指定してください。"));
            return;
        }

        String coloredName = ChatColor.translateAlternateColorCodes('&', message);

        if (!coloredName.equals(ChatColor.stripColor(coloredName))) {
            // データベースのニックネームに色を適用
            String currentNickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
            if (currentNickname == null || currentNickname.isEmpty()) {
                currentNickname = player.getName();
            }
            String updatedNickname = coloredName;

            NicknameDatabase.saveNickname(player.getUniqueId().toString(), updatedNickname);

            // 表示名を更新
            updateDisplayNamePrefix(player, updatedNickname);

            player.sendMessage(Component.text(ChatColor.GREEN + "名前の色を変更しました！: " + updatedNickname));
        } else {
            player.sendMessage(Component.text(ChatColor.RED + "無効なカラーコードです。例: &6Hello"));
        }

        waitingForColorInput.put(player.getUniqueId(), false);
    }

    public static void updateDisplayNamePrefix(Player player, String nickname) {
        CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);

        // LuckPermsのPrefixを取得
        String prefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";

        // Prefixとニックネームを結合
        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + nickname);

        // 表示名とTabリストの名前を更新
        player.displayName(Component.text(formattedName));
        player.playerListName(Component.text(formattedName));

        Bukkit.getLogger().info("ニックネームが設定されました " + player.getName() + ": " + formattedName);
    }
}

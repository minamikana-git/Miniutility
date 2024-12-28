package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    public void NicknameBase(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            handleNicknameInput(player, event.message());
        } else if (waitingForColorInput.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            handleColorInput(player, event.message());
        }
    }

    private void handleNicknameInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!message.isEmpty()) {
                // ニックネームをデータベースに保存
                NicknameDatabase.saveNickname(player.getUniqueId().toString(), message);

                // 表示名を更新
                updateDisplayNameWithPrefix(player, message);

                player.sendMessage(ChatColor.GREEN + "ニックネームを設定しました: " + message);
            } else {
                player.sendMessage(ChatColor.RED + "無効なニックネームです。");
            }
            waitingForNickname.put(player.getUniqueId(), false);
        });
    }

    private void handleColorInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        if (message.isEmpty()) {
            player.sendMessage(ChatColor.RED + "無効な入力です。カラーコードを指定してください。");
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
            updateDisplayNameWithPrefix(player, updatedNickname);

            player.sendMessage(ChatColor.GREEN + "名前の色を変更しました！: " + updatedNickname);
        } else {
            player.sendMessage(ChatColor.RED + "無効なカラーコードです。例: &6Hello");
        }

        waitingForColorInput.put(player.getUniqueId(), false);
    }

    private void updateDisplayNameWithPrefix(Player player, String nickname) {
        // LuckPermsのPrefixを取得
        CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
        String prefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";

        // プレイヤーの表示名を設定
        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + nickname);
        player.setDisplayName(formattedName);
        player.setPlayerListName(formattedName);
    }
}

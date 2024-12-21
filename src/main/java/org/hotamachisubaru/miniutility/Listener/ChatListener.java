package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    private static final Map<UUID, Boolean> waitingForNickname = new HashMap<>();
    private static final Map<UUID, Boolean> waitingForColorInput = new HashMap<>();

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
    public void onPlayerChat(AsyncChatEvent event) {
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

        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Miniutility"), () -> {
            if (!message.isEmpty()) {
                player.setDisplayName(message);
                player.setPlayerListName(message);
                player.sendMessage(ChatColor.GREEN + "ニックネームを設定しました！");
            } else {
                player.sendMessage(ChatColor.RED + "無効なニックネームです。");
            }
            waitingForNickname.put(player.getUniqueId(), false);
        });
    }

    private void handleColorInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("YourPluginName"), () -> {
            if (message.matches("^&[0-9a-fA-F]$")) {
                String coloredName = ChatColor.translateAlternateColorCodes('&', message) + player.getName();
                player.setDisplayName(coloredName);
                player.setPlayerListName(coloredName);
                player.sendMessage(ChatColor.GREEN + "名前の色を変更しました！");
            } else {
                player.sendMessage(ChatColor.RED + "無効なカラーコードです。例: &6");
            }
            waitingForColorInput.put(player.getUniqueId(), false);
        });
    }
}

package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {
    private final Map<UUID, Boolean> waitingForNickname = new HashMap<>();
    private final Map<Player, Boolean> waitingForColorInput = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            handleNicknameInput(player, message);
        } else if (waitingForColorInput.getOrDefault(player, false)) {
            event.setCancelled(true);
            handleColorInput(player, message);
        }
    }

    private void handleNicknameInput(Player player, String message) {
        if (message.isEmpty()) {
            player.sendMessage(ChatColor.RED + "無効なニックネームです。");
            return;
        }
        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', message));
        player.sendMessage(ChatColor.GREEN + "ニックネームを設定しました！");
        waitingForNickname.put(player.getUniqueId(), false);
    }

    private void handleColorInput(Player player, String message) {
        if (message.matches("^&[0-9a-fA-F]$")) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', message) + ChatColor.stripColor(player.getDisplayName());
            player.setDisplayName(coloredName);
            player.setPlayerListName(coloredName);
            player.sendMessage(ChatColor.GREEN + "色を変更しました！");
        } else {
            player.sendMessage(ChatColor.RED + "無効なカラーコードです。");
        }
        waitingForColorInput.put(player, false);
    }
}

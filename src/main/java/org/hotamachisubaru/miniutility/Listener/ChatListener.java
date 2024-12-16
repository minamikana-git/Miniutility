package org.hotamachisubaru.miniutility.Listener;

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
   // ニックネーム入力待機フラグ
    private static final Map<UUID, Boolean> waitingForNickname = new HashMap<UUID, Boolean>();

    // 色変更入力待機フラグ
    private static final Map<Player, Boolean> waitingForColorInput = new HashMap<>();
    // ニックネーム設定待機フラグをセット
    public static void setWaitingForNickname(Player player, boolean waiting) {
        waitingForNickname.put(player.getUniqueId(), waiting);
    }

    // 色変更設定待機フラグをセット
    public static void setWaitingForColorInput(Player player, boolean waiting) {
        waitingForColorInput.put(player, waiting);
    }

    // フラグを取得する（必要なら追加）
    public boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.getOrDefault(player, false);
    }

    public boolean isWaitingForNickname(Player player) {
        return waitingForNickname.getOrDefault(player, false);
    }

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

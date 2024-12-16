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
import java.util.logging.Logger;

public class ChatListener implements Listener {

    Logger plugin = Logger.getLogger("Miniutility");
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

        // ニックネームの入力待ち
        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            handleNicknameInput(player, event.message()); // Componentをそのまま渡す
        }
        // 色変更の入力待ち
        else if (waitingForColorInput.getOrDefault(player, false)) {
            event.setCancelled(true);
            handleColorInput(player, event.message()); // Componentをそのまま渡す
        }
    }


    private void handleNicknameInput(Player player, Component messageComponent) {
        // Component から文字列に変換
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        if (!message.isEmpty()) {
            player.setDisplayName(message);
            player.setPlayerListName(message);
            player.sendMessage(ChatColor.GREEN + "ニックネームを設定しました！");
        } else {
            player.sendMessage(ChatColor.RED + "無効なニックネームです。");
        }
        waitingForNickname.put(player.getUniqueId(), false);
    }


    private void handleColorInput(Player player, Component messageComponent) {
        // Component から文字列に変換
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        // カラーコードの検証と適用
        if (message.matches("^&[0-9a-fA-F]$") || message.matches("^&[0-9a-fA-F].*")) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', message)
                    + ChatColor.stripColor(player.getDisplayName());
            player.setDisplayName(coloredName);
            player.setPlayerListName(coloredName);
            player.sendMessage(ChatColor.GREEN + "名前の色を変更しました！");
        } else {
            player.sendMessage(ChatColor.RED + "無効なカラーコードです。例: &6");
        }
        waitingForColorInput.put(player, false);
    }

}

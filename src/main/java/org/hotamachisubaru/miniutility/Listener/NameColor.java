package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class NameColor implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // プレイヤーがカラーコードの入力待ち状態であるか確認
        if (waitingForColorInput.getOrDefault(player, false)) {
            event.setCancelled(true); // カラーコードの入力を通常のチャットメッセージとしては処理しない

            String message = event.getMessage();

            // カラーコードの形式が正しいかを確認（例：&6など）
            if (message.matches("^&[0-9a-fA-F]$")) {
                // プレイヤー名を指定されたカラーコードで設定（ニックネームが既に存在する場合はそれに色をつける）
                String currentNickname = player.getDisplayName();
                String coloredName = ChatColor.translateAlternateColorCodes('&', message) + ChatColor.stripColor(currentNickname);
                player.setDisplayName(coloredName);
                player.setPlayerListName(coloredName);
                player.sendMessage(ChatColor.GREEN.toString() + "プレイヤー名が " + message + " の色に変更されました！");
            } else {
                player.sendMessage(ChatColor.RED.toString() + "無効なカラーコードです。正しい形式で入力してください（例：&6）。");
            }

            // 入力待ち状態をリセット
            waitingForColorInput.put(player, false);
        }
    }

    public static Map<Player, Boolean> waitingForColorInput = new HashMap<>();
}

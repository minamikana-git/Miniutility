package org.hotamachisubaru.miniutility.Listener

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class NameColor : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        // プレイヤーがカラーコードの入力待ち状態であるか確認
        if (waitingForColorInput.getOrDefault(player, false)) {
            event.isCancelled = true // カラーコードの入力を通常のチャットメッセージとしては処理しない

            val message = event.message

            // カラーコードの形式が正しいかを確認（例：&6など）
            if (message.matches("^&[0-9a-fA-F]$".toRegex())) {
                // プレイヤー名を指定されたカラーコードで設定（ニックネームが既に存在する場合はそれに色をつける）
                val currentNickname = player.displayName
                val coloredName = ChatColor.translateAlternateColorCodes('&', message) + ChatColor.stripColor(currentNickname)
                player.setDisplayName(coloredName)
                player.setPlayerListName(coloredName)
                player.sendMessage(ChatColor.GREEN.toString() + "プレイヤー名が " + message + " の色に変更されました！")
            } else {
                player.sendMessage(ChatColor.RED.toString() + "無効なカラーコードです。正しい形式で入力してください（例：&6）。")
            }

            // 入力待ち状態をリセット
            waitingForColorInput[player] = false
        }
    }

    companion object {
        // プレイヤーがカラーコードの入力を待っているかどうかを記録するマップ
        var waitingForColorInput: MutableMap<Player, Boolean> = HashMap()
    }
}

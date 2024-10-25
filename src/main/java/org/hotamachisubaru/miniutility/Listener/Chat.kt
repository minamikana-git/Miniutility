package org.hotamachisubaru.miniutility.Listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class Chat : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val nickname = player.displayName // ニックネームを取得
        val message = event.message // チャットメッセージを取得

        // チャットのフォーマットをニックネーム付きに変更
        event.format = "$nickname: $message"
    }
}

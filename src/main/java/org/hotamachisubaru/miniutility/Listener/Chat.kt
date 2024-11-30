package org.hotamachisubaru.miniutility.Listener

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Chat : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.getPlayer()

        // プレイヤーの表示名 (Nickname)
        val nickname = player.displayName()

        // メッセージ (Component型として取得)
        val messageContent = event.message()

        // チャットフォーマットを設定
        event.renderer(ChatRenderer { source: Player?, sourceDisplayName: Component?, message: Component?, viewer: Audience? ->
            Component.text()
                .append(nickname) // ニックネーム部分
                .append(Component.text(": ")) // 区切り
                .append(message!!) // メッセージ本体
                .build()
        }
        )
    }
}

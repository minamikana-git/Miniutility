package org.hotamachisubaru.miniutility.Listener

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.hotamachisubaru.miniutility.Miniutility
import java.util.*

class NicknameInputListener(private val plugin: Miniutility) : Listener {
    private val waitingForNickname = HashMap<UUID, Boolean>()

    // プレイヤーがニックネームの入力を待っているかどうかを確認
    fun setWaitingForNickname(player: Player, waiting: Boolean) {
        waitingForNickname[player.uniqueId] = waiting
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val playerUUID = player.uniqueId

        // ニックネーム入力を待っている場合のみ処理
        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.isCancelled = true // チャットメッセージをキャンセル
            val nickname = event.message // プレイヤーが入力したメッセージをニックネームとして取得

            // ニックネームを設定
            plugin.nicknameConfig!!.setNickname(playerUUID, nickname)
            player.setDisplayName(nickname)
            player.setPlayerListName(nickname)

            player.sendMessage(ChatColor.GREEN.toString() + "ニックネームを " + nickname + " に設定しました。")

            // ニックネーム入力待ち状態を解除
            waitingForNickname.remove(playerUUID)
        }
    }
}

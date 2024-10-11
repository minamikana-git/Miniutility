package org.hotamachisubaru.miniutility.Nickname

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.hotamachisubaru.miniutility.Miniutility

class NicknameCommand(private val plugin: Miniutility) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます。")
            return true
        }

        val player = sender
        val uuid = player.uniqueId
        val config = plugin.nicknameConfig

        if (args.size == 0) {
            // ニックネームのリセット
            config!!.setNickname(uuid, null)
            player.setDisplayName(player.name)
            player.setPlayerListName(player.name) // プレイヤーリスト名もリセット
            player.sendMessage(ChatColor.GREEN.toString() + "ニックネームをリセットしました。")
        } else {
            // ニックネームの設定
            val nickname = java.lang.String.join(" ", *args)
            config!!.setNickname(uuid, nickname)
            player.setDisplayName(nickname)
            player.setPlayerListName(nickname) // プレイヤーリスト名も更新
            player.sendMessage(ChatColor.GREEN.toString() + "ニックネームを " + nickname + " に設定しました。")
        }

        return true // チャットには影響を与えない
    }
}
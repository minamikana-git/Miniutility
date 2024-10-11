package org.hotamachisubaru.miniutility

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.hotamachisubaru.miniutility.GUI.UtilityGUI

class UtilityCommand : CommandExecutor, Listener {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            // コマンドが "/mu" だった場合
            if (label.equals("menu", ignoreCase = true)) {
                // ユーティリティGUIを開く
                UtilityGUI.openUtilityGUI(sender)
                return true
            }
        } else {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます。")
        }

        return false
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val clickedItem = event.currentItem

        // GUI のタイトルが「便利箱」の場合のみ処理
        if (event.view.title == "便利箱") {
            event.isCancelled = true // クリック時にインベントリを操作できないようにする

            if (clickedItem == null || clickedItem.type == Material.AIR) {
                return  // 空のスロットをクリックした場合は何もしない
            }


        }
    }
}

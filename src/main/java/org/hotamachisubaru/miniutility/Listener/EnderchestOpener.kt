package org.hotamachisubaru.miniutility.Listener

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class EnderchestOpener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "便利箱") {  // GUI名が「便利箱」か確認
            event.isCancelled = true // インベントリの操作をキャンセル
            val player = event.whoClicked as Player
            val clickedItem = event.currentItem

            if (clickedItem == null || clickedItem.type == Material.AIR) {
                return
            }

            if (clickedItem.type == Material.ENDER_CHEST) {
                player.openInventory(player.enderChest) // エンダーチェストを開く
                //player.closeInventory();  // 便利箱を閉じる
            }
        }
    }
}

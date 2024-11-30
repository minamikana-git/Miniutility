package org.hotamachisubaru.miniutility.Listener

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class EnderchestOpener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.getView().getTitle() == "便利箱") { // 特定のGUIタイトルをチェック
            event.setCancelled(true) // 便利箱内での操作をキャンセル
            val player = event.getWhoClicked() as Player
            val clickedItem = event.getCurrentItem()

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return
            }

            if (clickedItem.getType() == Material.ENDER_CHEST) {
                player.openInventory(player.getEnderChest()) // エンダーチェストを開く
            }
        } else if (event.getView().getTitle().contains("エンダーチェスト")) {
            // エンダーチェストの操作を許可する
            event.setCancelled(false)
        }
    }
}

package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EnderchestOpener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // GUI名が「便利箱」か確認
        if ("便利箱".equals(event.getView().getTitle())) {
            event.setCancelled(true); // インベントリの操作をキャンセル
            Player player = (Player) event.getWhoClicked();

            // クリックしたアイテムを取得
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return; // アイテムが空の場合は処理を終了
            }

            // クリックしたアイテムがエンダーチェストか確認
            if (clickedItem.getType() == Material.ENDER_CHEST) {
                player.openInventory(player.getEnderChest()); // エンダーチェストを開く
            }
        }
    }
}

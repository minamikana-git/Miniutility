package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EnderchestOpener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if ("便利箱".equals(event.getView().getTitle())) {  // GUI名が「便利箱」か確認
            event.setCancelled(true); // インベントリの操作をキャンセル
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            if (event.getCurrentItem().getType() == Material.ENDER_CHEST) {
                player.openInventory(player.getEnderChest()); // エンダーチェストを開く
                //player.closeInventory();  // 便利箱を閉じる
            }
        }
    }
}

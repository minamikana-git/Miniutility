package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InstantCrafter implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if ("便利箱".equals(event.getView().getTitle())) {
            event.setCancelled(true); // インベントリの操作をキャンセル
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getCurrentItem().getType() == Material.CRAFTING_TABLE) {
                player.openWorkbench(null, true); // 作業台を開く
                // player.closeInventory();  // 便利箱を閉じる
            }
        }
    }
}

package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InstantCrafter implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        // 作業台アイテムをクリックしたか判定
        if (event.getCurrentItem().getType() == Material.CRAFTING_TABLE) {
            event.setCancelled(true);
            player.openWorkbench(player.getLocation(), true);
        }
    }
}

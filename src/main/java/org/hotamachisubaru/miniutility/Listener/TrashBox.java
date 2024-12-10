package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class TrashBox implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        // インベントリタイトルを文字列として取得
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // ゴミ箱の操作
        if (title.equals("ゴミ箱") && event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            clearTrash(player, event.getClickedInventory());
        }
    }

    private void clearTrash(Player player, Inventory inventory) {
        inventory.clear();
        player.sendMessage("ゴミ箱を空にしました！");
    }
}

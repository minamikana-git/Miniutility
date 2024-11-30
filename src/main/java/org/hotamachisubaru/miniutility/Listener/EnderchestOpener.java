package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EnderchestOpener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();



        // 便利箱GUI内のクリックイベントを処理
        if ("便利箱".equals(event.getView().getTitle())) { // 特定のGUIタイトルをチェック
            event.setCancelled(true); // 便利箱内での操作をキャンセル

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getCurrentItem().getType() == Material.ENDER_CHEST) {
                // プレイヤーのエンダーチェストを開く
                player.openInventory(player.getEnderChest());

            }
        } else if ("Ender Chest".equals(event.getView().getTitle())) {
            // エンダーチェスト内の操作を許可
            // 明示的にキャンセルを解除
            event.setCancelled(false);

        }
    }
}

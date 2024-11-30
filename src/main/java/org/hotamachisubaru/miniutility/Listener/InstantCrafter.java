package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InstantCrafter implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // "便利箱"内での処理
        if ("便利箱".equals(event.getView().getTitle())) {
            Player player = (Player) event.getWhoClicked();
            // 空アイテムをクリックした場合は処理を終了
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            // 作業台アイテムがクリックされた場合
            if (event.getCurrentItem().getType() == Material.CRAFTING_TABLE) {
                // インベントリ操作のキャンセル
                event.setCancelled(true);
                // クラフトテーブルを開く
                player.openWorkbench(null, true); // 作業台を開く
            }
        } else if ("Crafting".equals(event.getView().getTitle())) {
            // クラフトインベントリでの処理
            Player player = (Player) event.getWhoClicked();
            // 結果スロットでクリックされた場合
            if (event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.RESULT) {
                event.setCancelled(false); // 結果スロットの操作を許可
            }
        }
    }
}

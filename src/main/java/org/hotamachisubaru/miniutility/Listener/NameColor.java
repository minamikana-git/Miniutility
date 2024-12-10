package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

public class NameColor implements Listener {
    private static final Map<Player, Boolean> waitingForColorInput = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        // 名前の色変更アイテムをクリックしたか判定
        if (event.getCurrentItem().getType() == Material.GREEN_DYE) {
            event.setCancelled(true);
            promptForColorInput(player);
        }
    }

    private void promptForColorInput(Player player) {
        waitingForColorInput.put(player, true);
        player.sendMessage("チャットにカラーコードを入力してください（例：&6）。");
        player.closeInventory();
    }

    public static boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.getOrDefault(player, false);
    }

    public static void setWaitingForColorInput(Player player, boolean waiting) {
        waitingForColorInput.put(player, waiting);
    }
}

package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.hotamachisubaru.miniutility.Miniutility;

public class DeathListener implements Listener {

    private final Miniutility plugin;

    public DeathListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void saveDeathLocation(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // --- 1. 死亡地点を取得して、それを保存する ---
        Location deathLoc = player.getLocation().getBlock().getLocation().add(0, 1, 0);
        plugin.setDeathLocation(player.getUniqueId(), deathLoc);

        // --- 2. ダブルチェストを置く ---
        Block blockLeft  = deathLoc.getBlock();
        Block blockRight = deathLoc.clone().add(1, 0, 0).getBlock();

        blockLeft.setType(Material.CHEST);
        blockRight.setType(Material.CHEST);

        // --- チェスト設置後、1 tick遅延してからアイテムを詰める ---
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Chest chestLeft = (Chest) blockLeft.getState();

            Inventory targetInv = chestLeft.getInventory();
            // ダブルチェストかどうか判定
            if (targetInv.getHolder() instanceof DoubleChest doubleChest) {
                targetInv = doubleChest.getInventory();
            }

            PlayerInventory inv = player.getInventory();
            ItemStack[] mainContents = inv.getContents();
            ItemStack[] armorContents = inv.getArmorContents();
            ItemStack offHandItem = inv.getItemInOffHand();

            // アイテム投入
            for (ItemStack stack : mainContents) {
                if (stack != null && stack.getType() != Material.AIR) {
                    targetInv.addItem(stack.clone());
                }
            }
            for (ItemStack stack : armorContents) {
                if (stack != null && stack.getType() != Material.AIR) {
                    targetInv.addItem(stack.clone());
                }
            }
            if (offHandItem != null && offHandItem.getType() != Material.AIR) {
                targetInv.addItem(offHandItem.clone());
            }

            // 4. イベントのドロップをクリア
            event.getDrops().clear();

            // 5. プレイヤーの中身を空にする
            inv.clear();
            inv.setArmorContents(new ItemStack[]{null, null, null, null});
            inv.setItemInOffHand(null);

        }, 1L); // 1 tick後
    }
}

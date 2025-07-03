package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.hotamachisubaru.miniutility.Miniutility;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

public class DeathListener implements Listener {

    private final Miniutility plugin;

    public DeathListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void saveDeathLocation(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // 死亡地点（頭上1ブロック、ディメンション付き）保存
        Location deathLoc = player.getLocation().getBlock().getLocation().add(0, 1, 0);
        plugin.setDeathLocation(player.getUniqueId(), deathLoc);

        // 必要に応じてダブルチェスト設置
        FoliaUtil.runAtLocation(plugin, deathLoc, () -> {
            Block block1 = deathLoc.getBlock();
            Block block2 = block1.getRelative(1, 0, 0); // X+1側に並べる

            // 既にチェストがある場合は設置しない
            if (block1.getType() != Material.AIR || block2.getType() != Material.AIR) return;

            // まず両方Material.CHESTで設置
            block1.setType(Material.CHEST);
            block2.setType(Material.CHEST);

            // BlockDataでダブルチェストに設定
            org.bukkit.block.data.type.Chest chestData1 = (org.bukkit.block.data.type.Chest) block1.getBlockData();
            chestData1.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
            block1.setBlockData(chestData1);

            org.bukkit.block.data.type.Chest chestData2 = (org.bukkit.block.data.type.Chest) block2.getBlockData();
            chestData2.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
            block2.setBlockData(chestData2);

            // Chestインベントリへアイテムコピー（ダブルチェストで54スロット）
            if (block1.getState() instanceof Chest chest1) {
                PlayerInventory inv = player.getInventory();
                ItemStack[] contents = inv.getContents();
                for (int i = 0; i < Math.min(contents.length, chest1.getInventory().getSize()); i++) {
                    if (contents[i] != null) {
                        chest1.getInventory().setItem(i, contents[i].clone());
                    }
                }
                chest1.update();
            }
        });
    }
}

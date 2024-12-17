package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class CreeperExplosionRecover implements Listener {
    private final Map<Block, Material> explodedBlocks = new HashMap<>();
    private final Plugin plugin;

    public CreeperExplosionRecover(Plugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().forEach(block -> {
            explodedBlocks.put(block, block.getType());
        });

        // 正しいプラグインインスタンスを渡す
        Bukkit.getScheduler().runTaskLater(plugin, () -> restoreBlocks(), 1L);
    }

    private void restoreBlocks() {
        explodedBlocks.forEach((block, material) -> {
            block.setType(material);
        });
        explodedBlocks.clear();
    }

}

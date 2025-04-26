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
import org.hotamachisubaru.miniutility.Miniutility;

import java.util.ArrayList;

public class DeathListener implements Listener {

    private final Miniutility plugin;

    public DeathListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void saveDeathLocation(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation().getBlock().getLocation().add(0,1,0);
        plugin.setDeathLocation(player.getUniqueId(),deathLoc);

        Block block = deathLoc.getBlock();
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();

        for (ItemStack drop: new ArrayList<>(event.getDrops())) {
            chest.getBlockInventory().addItem(drop);
        }

        event.getDrops().clear();
    }
}

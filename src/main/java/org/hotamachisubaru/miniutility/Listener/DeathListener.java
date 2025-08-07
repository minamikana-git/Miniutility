package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.hotamachisubaru.miniutility.Miniutility;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;

public class DeathListener implements Listener {

    private final Miniutility plugin;

    public DeathListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void saveDeathLocation(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation().getBlock().getLocation().add(0, 1, 0);
        plugin.setDeathLocation(player.getUniqueId(), deathLoc);
        // チェスト設置処理などは現状通り
    }

    // ワープ両対応
    public static void teleportToDeathLocation(Player player, Location location) {
        if (APIVersionUtil.isModern()) {
            try {
                Player.class.getMethod("teleportAsync", Location.class).invoke(player, location);
            } catch (Throwable e) {
                player.teleport(location);
            }
        } else {
            player.teleport(location);
        }
    }
    public static Location getLastDeathLocation(Player player, Miniutility plugin) {
        if (APIVersionUtil.isModern()) {
            try {
                Object loc = Player.class.getMethod("getLastDeathLocation").invoke(player);
                if (loc instanceof Location) return (Location) loc;
            } catch (Throwable ignore) {}
        }
        return plugin.getDeathLocation(player.getUniqueId());
    }
}

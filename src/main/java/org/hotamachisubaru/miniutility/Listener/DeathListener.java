package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.hotamachisubaru.miniutility.Miniutility;

public class DeathListener implements Listener {

    private final Miniutility plugin;

    public DeathListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void saveDeathLocation(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // 死亡地点（頭上1ブロック）を保存するだけ
        Location deathLoc = player.getLocation().getBlock().getLocation().add(0, 1, 0);
        plugin.setDeathLocation(player.getUniqueId(), deathLoc);
        // チェスト設置やアイテム処理は一切なし
    }
}

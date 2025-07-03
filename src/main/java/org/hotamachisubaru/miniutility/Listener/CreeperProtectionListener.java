package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.hotamachisubaru.miniutility.Miniutility;

public class CreeperProtectionListener implements Listener {

    private final Miniutility plugin;
    private boolean creeperProtectionEnabled = true;

    public CreeperProtectionListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!creeperProtectionEnabled) return;
        if (event.getEntity() instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    // トグル制御
    public boolean toggleCreeperProtection() {
        creeperProtectionEnabled = !creeperProtectionEnabled;
        return false;
    }
    public boolean isCreeperProtectionEnabled() {
        return creeperProtectionEnabled;
    }
}

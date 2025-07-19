package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

public class CreeperProtectionListener implements Listener {
    private final MiniutilityLoader plugin;
    private boolean creeperProtectionEnabled = true;

    public CreeperProtectionListener(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!creeperProtectionEnabled) return;
        if (event.getEntity() instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    public boolean toggleCreeperProtection() {
        creeperProtectionEnabled = !creeperProtectionEnabled;
        return creeperProtectionEnabled;
    }
    public boolean isCreeperProtectionEnabled() {
        return creeperProtectionEnabled;
    }
}

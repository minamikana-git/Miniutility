package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class CreeperProtectionListener implements Listener {
    private boolean enabled = true;

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (enabled && event.getEntity() instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    /** トグルして新状態(true=有効/false=無効)を返す */
    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public boolean toggleCreeperProtection() { enabled = !enabled; return enabled; }

    public boolean isEnabled() {
        return enabled;
    }
    public boolean isCreeperProtectionEnabled() { return enabled; }
}

package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class CreeperProtectionListener implements Listener {
    private boolean isCreeperProtectionEnabled = false;

    public boolean isCreeperProtectionEnabled() {
        return isCreeperProtectionEnabled;
    }

    public boolean toggleCreeperProtection() {
        this.isCreeperProtectionEnabled = !this.isCreeperProtectionEnabled;
        return false;
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (this.isCreeperProtectionEnabled && event.getEntity() instanceof Creeper) {
            event.blockList().clear(); // クリーパー爆破のブロック破壊を防ぐ
        }
    }
}
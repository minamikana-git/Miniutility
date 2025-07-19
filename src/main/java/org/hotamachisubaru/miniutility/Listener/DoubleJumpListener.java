package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DoubleJumpListener implements Listener {

    private final Plugin plugin;
    private final Set<UUID> canDoubleJump = new HashSet<>();
    private final Map<UUID, Boolean> doubleJumpEnabledMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> doubleJumpCooldowns = new ConcurrentHashMap<>();

    public DoubleJumpListener(Plugin plugin) {
        this.plugin = plugin;
    }

    // 有効判定
    public boolean isDoubleJumpEnabled(UUID uuid) {
        return doubleJumpEnabledMap.getOrDefault(uuid, true); // デフォルトON
    }

    // ON/OFFトグル
    public boolean toggleDoubleJump(UUID uuid) {
        boolean enabled = !isDoubleJumpEnabled(uuid);
        doubleJumpEnabledMap.put(uuid, enabled);
        return enabled; // true=ON, false=OFF
    }

    // 地上に着いたら2段ジャンプを再セット
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!isDoubleJumpEnabled(uuid)) {
            if (player.getAllowFlight()) player.setAllowFlight(false);
            canDoubleJump.remove(uuid);
            return;
        }

        if (!player.isOnGround()
                || player.getGameMode() != GameMode.SURVIVAL
                || player.getAllowFlight()) {
            return;
        }
        player.setAllowFlight(true);
        canDoubleJump.add(uuid);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        if (doubleJumpCooldowns.containsKey(uuid) && doubleJumpCooldowns.get(uuid) > now) {
            double left = (doubleJumpCooldowns.get(uuid) - now) / 1000.0;
            player.sendActionBar(Component.text(String.format("クールタイム: %.1f秒", left)));
            event.setCancelled(true);
            return;
        }

        if (player.getGameMode() != GameMode.SURVIVAL
                || !canDoubleJump.contains(uuid)
                || !isDoubleJumpEnabled(uuid)) {
            return;
        }

        event.setCancelled(true);
        player.setAllowFlight(false);
        canDoubleJump.remove(uuid);
        doubleJumpCooldowns.put(uuid, now + 1000L);

        FoliaUtil.runAtPlayer(plugin, player, () -> {
            player.setVelocity(player.getVelocity().setY(0.42));
        });
    }
}

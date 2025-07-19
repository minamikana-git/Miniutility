package org.hotamachisubaru.miniutility.Listener;

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
    // ★追加: プレイヤーごとのON/OFF状態を保持
    private static final Map<UUID, Boolean> doubleJumpEnabledMap = new ConcurrentHashMap<>();

    public DoubleJumpListener(Plugin plugin) {
        this.plugin = plugin;
    }

    // 2段ジャンプ有効判定
    public static boolean isDoubleJumpEnabled(UUID uuid) {
        return doubleJumpEnabledMap.getOrDefault(uuid, true); // デフォルトON
    }

    // ON/OFFトグル
    public static void toggleDoubleJump(Player player) {
        UUID uuid = player.getUniqueId();
        doubleJumpEnabledMap.put(uuid, !isDoubleJumpEnabled(uuid));
    }

    // 地上に着いたら2段ジャンプを再セット
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnGround()
                || player.getGameMode() != GameMode.SURVIVAL
                || player.getAllowFlight()
                || !isDoubleJumpEnabled(player.getUniqueId())) { // ←有効時のみ
            return;
        }
        player.setAllowFlight(true);
        canDoubleJump.add(player.getUniqueId());
    }

    // 2段ジャンプ本体
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.SURVIVAL
                || !canDoubleJump.contains(player.getUniqueId())
                || !isDoubleJumpEnabled(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        player.setAllowFlight(false);
        canDoubleJump.remove(player.getUniqueId());

        FoliaUtil.runAtPlayer(plugin, player, () -> {
            player.setVelocity(player.getVelocity().setY(0.8));
        });
    }
}

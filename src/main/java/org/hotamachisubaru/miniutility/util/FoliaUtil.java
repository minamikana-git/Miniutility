package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FoliaUtil {

    private static final boolean IS_FOLIA = isFolia();

    private static boolean isFolia() {
        try {
            Server.class.getMethod("getGlobalRegionScheduler");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    // Player単位でのスケジューリング（Paper/Folia両対応）
    public static void runAtPlayer(Player player, Runnable task) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(player.getServer().getPluginManager().getPlugin("Miniutility"), ignored -> task.run(), player);
        } else {
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Miniutility"), task);
        }
    }

    // サーバー単位でのスケジューリング
    public static void runAsync(Runnable task) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().execute(task);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("Miniutility"), task);
        }
    }
}

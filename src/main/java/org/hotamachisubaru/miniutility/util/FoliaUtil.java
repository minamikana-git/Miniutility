package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FoliaUtil {

    private static final boolean IS_FOLIA = isFolia0();

    private static boolean isFolia0() {
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

    // プレイヤー対象のタスク（Paper/Folia両対応）
    public static void runAtPlayer(Player player, Runnable task) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Miniutility");
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    // サーバータスク
    public static void runAsync(Runnable task) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Miniutility");
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}

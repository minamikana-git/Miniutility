package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

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

    public static void runAtPlayer(Plugin plugin, Player player, Consumer<Player> action) {
        if (IS_FOLIA) {
            try {
                // FoliaのPlayerSchedulerをリフレクションで呼び出す
                Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
                scheduler.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class)
                        .invoke(scheduler, plugin, (java.util.function.Consumer<?>) (scheduledTask -> action.accept(player)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    action.accept(player);
                }
            }.runTask(plugin);
        }
    }


    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            try {
                // FoliaのRegionSchedulerをリフレクションで呼ぶ（Paper環境でもエラー出ない！）
                Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(Bukkit.getServer());
                try {
                    // 新API(runメソッド)
                    regionScheduler.getClass().getMethod(
                            "run",
                            Plugin.class,
                            location.getWorld().getClass(),
                            Location.class,
                            java.util.function.Consumer.class
                    ).invoke(regionScheduler, plugin, location.getWorld(), location, (java.util.function.Consumer<?>) (scheduledTask -> task.run()));
                } catch (NoSuchMethodException e) {
                    // 旧API(executeメソッド)
                    regionScheduler.getClass().getMethod(
                            "execute",
                            Plugin.class,
                            location.getWorld().getClass(),
                            int.class,
                            int.class,
                            java.util.function.Consumer.class
                    ).invoke(regionScheduler, plugin, location.getWorld(), location.getBlockX(), location.getBlockZ(), (java.util.function.Consumer<?>) (scheduledTask -> task.run()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    task.run();
                }
            }.runTask(plugin);
        }
    }

}

package org.hotamachisubaru.miniutility.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

import static org.bukkit.Bukkit.*;

public final class FoliaUtil {
    private static final boolean IS_FOLIA = detectFolia();

    private static boolean detectFolia() {
        try {
            // Folia: Server#getGlobalRegionScheduler が存在
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runNow(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            getGlobalRegionScheduler().execute(plugin, task);
        } else {
            getScheduler().runTask(plugin, task);
        }
    }

    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
           getGlobalRegionScheduler().runDelayed(plugin, (Consumer<ScheduledTask>) task, delayTicks);
        } else {
            getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runAtPlayer(Plugin plugin, java.util.UUID uuid, Runnable task) {
        if (IS_FOLIA) {
            getRegionScheduler().execute(plugin, getPlayer(uuid).getLocation(), task);
        } else {
            getScheduler().runTask(plugin, task);
        }
    }
}

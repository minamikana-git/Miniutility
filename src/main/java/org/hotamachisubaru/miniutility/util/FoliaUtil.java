package org.hotamachisubaru.miniutility.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.function.Consumer;

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

    public static void runNow(org.bukkit.plugin.Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            org.bukkit.Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runLater(org.bukkit.plugin.Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            org.bukkit.Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (Consumer<ScheduledTask>) task, delayTicks);
        } else {
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runAtPlayer(org.bukkit.plugin.Plugin plugin, java.util.UUID uuid, Runnable task) {
        if (IS_FOLIA) {
            org.bukkit.Bukkit.getRegionScheduler().execute(plugin, org.bukkit.Bukkit.getPlayer(uuid).getLocation(), task);
        } else {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, task);
        }
    }
}

package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class FoliaUtil {
    public static void runNow(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
    public static void runAtPlayer(Plugin plugin, UUID uuid, Runnable task) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;

        Bukkit.getScheduler().runTask(plugin, task);
    }
}
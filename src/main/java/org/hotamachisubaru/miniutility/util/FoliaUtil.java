package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class FoliaUtil {
    private static final boolean IS_FOLIA;
    static {
        boolean f;
        try { Class.forName("io.papermc.paper.threadedregions.RegionizedServer"); f = true; }
        catch (ClassNotFoundException e) { f = false; }
        IS_FOLIA = f;
    }
    private FoliaUtil(){}

    public static void runNow(Plugin plugin, Runnable task) {
        if (IS_FOLIA) Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        else Bukkit.getScheduler().runTask(plugin, task);
    }
    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) Bukkit.getGlobalRegionScheduler().runDelayed(plugin, x -> task.run(), delayTicks);
        else Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
    public static void runAtPlayer(Plugin plugin, UUID uuid, Runnable task) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;
        if (IS_FOLIA) Bukkit.getRegionScheduler().execute(plugin, p.getLocation(), task);
        else Bukkit.getScheduler().runTask(plugin, task);
    }
}
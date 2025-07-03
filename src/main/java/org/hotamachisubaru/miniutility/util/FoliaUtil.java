package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Folia/Paper両対応ユーティリティ
 * Folia検出時はGlobalRegionScheduler等で分岐
 */
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

    public static boolean isFoliaServer() {
        return IS_FOLIA;
    }

    /** プレイヤーのスレッドでタスクを実行 */
    public static void runAtPlayer(Plugin plugin, Player player, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getServer().getRegionScheduler().run(plugin, player.getLocation(), (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /** 指定ロケーションでタスクを実行 */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getServer().getRegionScheduler().run(plugin, location, (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /** 任意スレッド・ディレイ実行（tick指定） */
    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (ignored) -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /** 非同期タスク */
    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /** 非同期＋遅延タスク */
    public static void runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (ignored) -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }
}

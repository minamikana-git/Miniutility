package org.hotamachisubaru.miniutility.util;

import org.bukkit.Bukkit;

public class APIVersionUtil {
    public static int getMajorVersion() {
        String version = Bukkit.getBukkitVersion();
        String[] parts = version.split("\\.");
        try {
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
    public static boolean isAtLeast(int majorVersion) {
        return getMajorVersion() >= majorVersion;
    }
    public static boolean isModern() {
        return getMajorVersion() >= 20; // 1.20以降
    }
}

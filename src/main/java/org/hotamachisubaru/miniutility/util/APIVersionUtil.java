package org.hotamachisubaru.miniutility.util;

public class APIVersionUtil {
    // Paper新API (paper-plugin.yml対応) 判定
    public static boolean isPaperAPI() {
        try {
            // 1.19.3以降のPaperではこのクラスが存在
            Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

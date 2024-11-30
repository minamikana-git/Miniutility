package org.hotamachisubaru.miniutility;

import org.bukkit.ChatColor;

// Constants.java として保存
public class Constants {
    public static final String UTILITY_BOX_TITLE = "便利箱";
    public static final String TRASH_BOX_TITLE = createColoredString(ChatColor.GREEN, "ゴミ箱");
    public static final String TRASH_CONFIRM_TITLE = createColoredString(ChatColor.RED, "本当に捨てますか？");

    private static String createColoredString(ChatColor color, String text) {
        return color.toString() + text;
    }
}
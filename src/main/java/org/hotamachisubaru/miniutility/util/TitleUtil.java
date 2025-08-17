package org.hotamachisubaru.miniutility.util;

public class TitleUtil {
    private static final boolean HAS_COMPONENT = has("net.kyori.adventure.text.Component");
    private static boolean has(String c){ try{ Class.forName(c); return true;}catch(ClassNotFoundException e){return false;} }

    public static String getTitle(org.bukkit.inventory.InventoryView view) {
        try {
            if (HAS_COMPONENT) {
                var comp = view.title(); // Paper: Component
                var serializer = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText();
                return serializer.serialize(comp).trim();
            }
        } catch (Throwable ignored) {}
        return view.getTitle().trim(); // 旧API
    }
}

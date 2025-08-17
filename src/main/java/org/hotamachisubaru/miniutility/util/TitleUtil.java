
package org.hotamachisubaru.miniutility.util;

import java.lang.reflect.Method;

public final class TitleUtil {
    private TitleUtil() {}

    /** どのAPIでも落ちない安全タイトル取得 */
    public static String getTitle(Object view) {
        if (view == null) return "";

        // 1) Paper新式: InventoryView#title() -> Component
        try {
            Method title = view.getClass().getMethod("title");
            Object component = title.invoke(view);
            String plain = toPlain(component);
            if (plain != null) return plain;
        } catch (NoSuchMethodException ignore) {
            // 次のパスへ
        } catch (Throwable t) {
            // 失敗したら次へ
        }

        // 2) 旧式/汎用: InventoryView#getTitle() -> String
        try {
            Method getTitle = view.getClass().getMethod("getTitle");
            Object str = getTitle.invoke(view);
            return str == null ? "" : String.valueOf(str);
        } catch (Throwable t) {
            return "";
        }
    }

    /** Adventure を反射で使ってプレーン文字列化 */
    private static String toPlain(Object component) {
        if (component == null) return "";
        try {
            Class<?> compClazz = Class.forName("net.kyori.adventure.text.Component");
            Class<?> serClazz  = Class.forName("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
            Object serializer  = serClazz.getMethod("plainText").invoke(null);
            Object res         = serClazz.getMethod("serialize", compClazz).invoke(serializer, component);
            return res == null ? "" : res.toString();
        } catch (Throwable ignore) {
            return component.toString();
        }
    }
}

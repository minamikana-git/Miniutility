package org.hotamachisubaru.miniutility.Bridge;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

public final class ChatBridge implements Listener {
    private static final String PAPER_EVENT = "io.papermc.paper.event.player.AsyncChatEvent";

    private static boolean isPaperAsyncChat(Event e) {
        return e != null && e.getClass().getName().equals(PAPER_EVENT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnyChat(Event e) {
        if (!isPaperAsyncChat(e)) return;

        try {
            // Player
            Method getPlayer = e.getClass().getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(e);

            // message() → Component → 平文（失敗時は toString）
            Method msgGetter = e.getClass().getMethod("message");
            Object component = msgGetter.invoke(e);
            String plain = toPlainText(component);

            // 待機フラグの共通処理（消費したらキャンセル）
            boolean consumed = org.hotamachisubaru.miniutility.Listener.Chat.tryHandleWaitingInput(player, plain);
            if (consumed) {
                Method setCancelled = e.getClass().getMethod("setCancelled", boolean.class);
                setCancelled.invoke(e, true);
            }

            // ここでは通常チャットの加工は行わない（Paper側に任せる）
            // 必要なら setMessage(Component) を反射で差し替え可能

        } catch (Throwable t) {
            Bukkit.getLogger().warning("エラーが発生しました。開発者にissueを送ってください。E301");
        }
    }

    private static String toPlainText(Object component) {
        try {
            Class<?> comp = Class.forName("net.kyori.adventure.text.Component");
            Class<?> ser  = Class.forName("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
            Method plainText = ser.getMethod("plainText");
            Object serializer = plainText.invoke(null);
            Method serialize = ser.getMethod("serialize", comp);
            Object res = serialize.invoke(serializer, component);
            return res == null ? "" : res.toString();
        } catch (Throwable ignore) {
            return component == null ? "" : component.toString();
        }
    }
}

package org.hotamachisubaru.miniutility.Bridge;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hotamachisubaru.miniutility.Listener.Chat;

import java.lang.reflect.Method;

public final class ChatBridge implements Listener {
    private static final String PAPER_EVENT = "io.papermc.paper.event.player.AsyncChatEvent";

    private static boolean isPaperAsyncChat(Event e) {
        return e != null && e.getClass().getName().equals(PAPER_EVENT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLegacyAsyncChat(AsyncPlayerChatEvent e) {
        // Paper新式がある環境では二重送信を避けるためスキップ
        if (hasPaperAsyncChat()) return;

        final Player player = e.getPlayer();
        final String plain = e.getMessage(); // ← 文字列でOK。反射不要

        // 待機フラグの共通処理（消費したらキャンセル）
        if (Chat.tryHandleWaitingInput(player, plain)) {
            e.setCancelled(true);
        }

        // ここで必要なら setFormat(...) などの通常チャット処理
        // e.setFormat(display + ChatColor.RESET + " » %2$s");
    }

    /**
     * Paper の AsyncChatEvent が存在するか
     */// ここでは通常チャットの加工は行わない（Paper側に任せる）

    private static boolean hasPaperAsyncChat() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Throwable t) {
            Bukkit.getLogger().warning("エラーが発生しました。開発者にissueを送ってください。E301");
            return false;
        }
    }

    private static String toPlainText(Object component) {
        try {
            Class<?> comp = Class.forName("net.kyori.adventure.text.Component");
            Class<?> ser = Class.forName("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
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








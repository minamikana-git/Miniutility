package org.hotamachisubaru.miniutility.Bridge;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatBridge implements Listener {

    // Paper (1.19+) の AsyncChatEvent があるか？
    private static final boolean HAS_PAPER_ASYNC_CHAT = has("io.papermc.paper.event.player.AsyncChatEvent");

    private static boolean has(String clazz) {
        try { Class.forName(clazz); return true; } catch (ClassNotFoundException e) { return false; }
    }

    // Paper 新式
    @EventHandler(ignoreCancelled = true)
    public void onPaperAsyncChat(AsyncChatEvent e) {
        if (!HAS_PAPER_ASYNC_CHAT) return; // 古いサーバでは無視
        var player = e.getPlayer();
        // ここにあなたの処理（MiniutilityのニックネームやPrefix結合など）
        // e.message() は Component、送信書き換えは e.message(Component) で。
    }

    // 旧式（Spigot互換）
    @EventHandler(ignoreCancelled = true)
    public void onLegacyAsyncChat(AsyncPlayerChatEvent e) {
        if (HAS_PAPER_ASYNC_CHAT) return; // 新式があるなら旧式は触らない
        var player = e.getPlayer();
        String msg = e.getMessage();
        // ここにあなたの処理。必要なら e.setMessage(…)。
    }
}


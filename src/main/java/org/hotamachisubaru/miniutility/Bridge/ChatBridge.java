package org.hotamachisubaru.miniutility.Bridge;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.hotamachisubaru.miniutility.Listener.Chat;

public final class ChatBridge implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();
        final String plain = e.getMessage();

        // 待機フラグの共通処理（消費したらキャンセル）
        if (Chat.tryHandleWaitingInput(player, plain)) {
            e.setCancelled(true);
        }

        // 必要ならここでチャット加工
        // e.renderer((sender, viewer, message) -> Component.text(display).append(Component.text(" » ")).append(message));
    }
}








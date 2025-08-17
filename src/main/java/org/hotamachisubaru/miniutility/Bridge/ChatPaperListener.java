package org.hotamachisubaru.miniutility.Bridge;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.hotamachisubaru.miniutility.Listener.Chat;

public final class ChatPaperListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPaperAsyncChat(AsyncChatEvent e) {
        final Player player = e.getPlayer();
        final String plain = PlainTextComponentSerializer.plainText().serialize(e.message());

        // 旧式共通処理に合流（消費したらキャンセル）
        boolean consumed = Chat.tryHandleWaitingInput(player, plain);
        if (consumed) {
            e.setCancelled(true);
        }
        // ※通常チャットの書き換えが必要なら e.message(Component.text(...)) で差し替え可
    }
}

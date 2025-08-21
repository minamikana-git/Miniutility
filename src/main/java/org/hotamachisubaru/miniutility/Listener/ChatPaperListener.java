// Bridge/ChatPaperListener.java
package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ChatPaperListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPaperAsyncChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String plain = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (Chat.tryHandleWaitingInput(player, plain)) {
            e.setCancelled(true);
        }
    }
}

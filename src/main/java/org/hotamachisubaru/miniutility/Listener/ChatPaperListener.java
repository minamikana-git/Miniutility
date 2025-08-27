// Bridge/ChatPaperListener.java
package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatPaperListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String plain = e.getMessage();
        if (Chat.tryHandleWaitingInput(player, plain)) {
            e.setCancelled(true);
        }
    }
}

package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;

public class Chat implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        String nickname = player.getDisplayName(); // ニックネームを取得
        Component message = event.message(); // チャットメッセージを取得

        // チャットのフォーマットをニックネーム付きに変更
        event.renderer((sender, sourceDisplayName, msg, viewer) -> Component.text(nickname + ": ").append(msg));
    }
}
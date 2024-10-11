package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String nickname = player.getDisplayName();  // ニックネームを取得
        String message = event.getMessage();  // チャットメッセージを取得

        // チャットのフォーマットをニックネーム付きに変更
        event.setFormat(nickname + ": " + message);
    }
}

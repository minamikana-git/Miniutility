package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hotamachisubaru.miniutility.Miniutility;

import java.util.HashMap;
import java.util.UUID;

public class NicknameInputListener implements Listener {

    private final Miniutility plugin;
    private final HashMap<UUID, Boolean> waitingForNickname = new HashMap<>();

    public NicknameInputListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    // プレイヤーがニックネームの入力を待っているかどうかを確認
    public void setWaitingForNickname(Player player, boolean waiting) {
        waitingForNickname.put(player.getUniqueId(), waiting);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // ニックネーム入力を待っている場合のみ処理
        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);  // チャットメッセージをキャンセル
            String nickname = event.getMessage();  // プレイヤーが入力したメッセージをニックネームとして取得

            // ニックネームを設定
            plugin.getNicknameConfig().setNickname(playerUUID, nickname);
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);

            player.sendMessage(ChatColor.GREEN + "ニックネームを " + nickname + " に設定しました。");

            // ニックネーム入力待ち状態を解除
            waitingForNickname.remove(playerUUID);
        }
    }
}

package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.hotamachisubaru.miniutility.Miniutility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Nickname implements Listener {
    private final Miniutility plugin;
    private final Map<UUID, Boolean> waitingForNickname = new HashMap<>();

    public Nickname(Miniutility plugin) {
        this.plugin = plugin;
    }

    // プレイヤーがニックネームの入力を待っているかどうかを確認
    public void setWaitingForNickname(Player player, boolean waiting) {
        waitingForNickname.put(player.getUniqueId(), waiting);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // ニックネーム入力を待っている場合のみ処理
        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true); // チャットメッセージをキャンセル

            // メッセージを文字列として取得
            Component messageComponent = event.message();
            String nickname = PlainTextComponentSerializer.plainText().serialize(messageComponent);

            // ニックネームを設定
            plugin.getNicknameConfig().setNickname(playerUUID, nickname);
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);

            player.sendMessage(ChatColor.GREEN + "ニックネームを " + ChatColor.YELLOW + nickname + ChatColor.GREEN + " に設定しました。");

            // ニックネーム入力待ち状態を解除
            waitingForNickname.remove(playerUUID);
        }
    }
}

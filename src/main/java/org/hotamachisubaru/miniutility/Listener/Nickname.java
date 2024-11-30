package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.hotamachisubaru.miniutility.Nickname.NicknameConfig;
import java.util.Map;
import java.util.UUID;

public class Nickname implements Listener {
    private final Map<UUID, Boolean> waitingForNickname;
    private final NicknameConfig config;

    public Nickname(Map<UUID, Boolean> waitingForNickname, NicknameConfig config) {
        this.waitingForNickname = waitingForNickname;
        this.config = config; // NicknameConfigのインスタンスを受け取る
    }

    public void setNickname(UUID uuid, String nickname) {
        config.setNickname(uuid, nickname);
        waitingForNickname.put(uuid, true); // 入力待ち状態を更新
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (waitingForNickname.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);

            // チャット入力されたニックネームを取得
            String nickname = PlainTextComponentSerializer.plainText().serialize(event.message());

            // ニックネームを設定
            config.setNickname(player.getUniqueId(), nickname); // 保存
            player.displayName(Component.text(nickname, NamedTextColor.WHITE));
            player.playerListName(Component.text(nickname, NamedTextColor.WHITE));

            // フィードバックメッセージ
            player.sendMessage(
                    Component.text("ニックネームを ")
                            .append(Component.text(nickname, NamedTextColor.WHITE))
                            .append(Component.text(" に設定しました！"))
            );

            // 状態をリセット
            waitingForNickname.remove(player.getUniqueId());
        }
    }
    public void setWaitingForNickname(Player player, boolean waiting) {
        UUID uuid = player.getUniqueId();
        waitingForNickname.put(uuid, waiting);
    }
}

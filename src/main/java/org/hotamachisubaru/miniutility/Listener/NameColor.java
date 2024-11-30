package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class NameColor implements Listener {

    public static final Map<Player, Boolean> waitingForColorInput = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // プレイヤーがカラーコードの入力待ち状態であるか確認
        if (waitingForColorInput.getOrDefault(player, false)) {
            event.setCancelled(true); // 通常のチャットメッセージ処理をキャンセル

            String message = PlainTextComponentSerializer.plainText().serialize(event.message()); // チャット入力を文字列に変換

            // カラーコードの形式が正しいかを確認（例：&6など）
            if (message.matches("^&[0-9a-fA-F]$")) {
                // プレイヤー名を指定されたカラーコードで設定
                String strippedName = PlainTextComponentSerializer.plainText().serialize(player.displayName()); // 現在の名前を文字列化
                String coloredName = ChatColor.translateAlternateColorCodes('&', message) + ChatColor.stripColor(strippedName);

                // プレイヤーの表示名とリスト名を設定
                Component coloredComponent = Component.text(coloredName);
                player.displayName(coloredComponent);
                player.playerListName(coloredComponent);

                // メッセージ送信
                player.sendMessage(
                        Component.text("プレイヤー名が ")
                                .append(Component.text(coloredName).color(NamedTextColor.GREEN))
                                .append(Component.text(" に変更されました！"))
                );
            } else {
                // 無効なカラーコードの場合
                player.sendMessage(
                        Component.text("無効なカラーコードです。正しい形式で入力してください（例：&6）。", NamedTextColor.RED)
                );
            }

            // 入力待ち状態をリセット
            waitingForColorInput.put(player, false);
        }
    }
}

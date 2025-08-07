package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chatリスナー: 1.17.1～1.21.x/Paper両対応
 */
public class Chat implements Listener {

    // --- チャット待機フラグなど ---
    private static final Map<UUID, Boolean> waitingForExpInput = new ConcurrentHashMap<>();

    public static void setWaitingForExpInput(Player player, boolean waiting) {
        waitingForExpInput.put(player.getUniqueId(), waiting);
    }

    public static boolean isWaitingForExpInput(Player player) {
        return waitingForExpInput.getOrDefault(player.getUniqueId(), false);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // --- チャット入力待機中フラグ ---
        if (isWaitingForExpInput(player)) {
            event.setCancelled(true);
            waitingForExpInput.put(player.getUniqueId(), false);
            // 数値として認識できるか判定し、レベル変更
            try {
                int change = Integer.parseInt(event.getMessage().trim());
                int newLevel = Math.max(player.getLevel() + change, 0);
                player.setLevel(newLevel);
                player.sendMessage(Component.text("経験値レベルを " + newLevel + " に変更しました。").color(NamedTextColor.GREEN));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("数値を入力してください。").color(NamedTextColor.RED));
            }
            return;
        }

        // --- 通常チャットの表示制御（LuckPermsプレフィックス＋ニックネーム） ---
        String prefix = "";
        try {
            // LuckPerms（v5 API）のプレフィックス取得
            var meta = net.luckperms.api.LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
            prefix = meta.getPrefix() == null ? "" : meta.getPrefix();
        } catch (Throwable e) {
            // LuckPerms未導入時はprefix無し
        }

        String nickname = NicknameManager.getDisplayName(player);
        String displayName = prefix + nickname;

        // 1.19.1以降のAdventureAPIなら
        if (APIVersionUtil.isAtLeast(19)) {
            event.viewers().forEach(viewer -> {
                viewer.sendMessage(
                        Component.text(displayName + " » " + event.getMessage())
                                .color(NamedTextColor.WHITE)
                );
            });
            event.setCancelled(true);
        } else {
            // 旧APIは元のsetFormatで
            event.setFormat(displayName + " » " + event.getMessage());
        }
    }
}

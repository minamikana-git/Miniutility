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

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Chatリスナー: 1.17.1～1.21.x/Paper両対応
 */
public class Chat implements Listener {

    // --- チャット待機フラグなど ---
    private static final Map<UUID, Boolean> waitingForNickname = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingForColorInput = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingForExpInput = new ConcurrentHashMap<>();

    public static void setWaitingForNickname(Player player, boolean waiting) {
        waitingForNickname.put(player.getUniqueId(), waiting);
    }

    public static boolean isWaitingForNickname(Player player) {
        return waitingForNickname.getOrDefault(player.getUniqueId(), false);
    }

    public static void setWaitingForColorInput(Player player, boolean waiting) {
        waitingForColorInput.put(player.getUniqueId(), waiting);
    }

    public static boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.getOrDefault(player.getUniqueId(), false);
    }

    public static boolean isWaitingForExpInput(Player player) {
        return waitingForExpInput.getOrDefault(player.getUniqueId(), false);
    }

    public static void setWaitingForExpInput(Player player, boolean waiting) {
        waitingForExpInput.put(player.getUniqueId(), waiting);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // --- チャット入力待機中フラグ ---
        if (isWaitingForExpInput(player)) {
            event.setCancelled(true);
            waitingForExpInput.put(player.getUniqueId(), false);
            try {
                int change = Integer.parseInt(event.getMessage().trim());
                int newLevel = Math.max(player.getLevel() + change, 0);
                player.setLevel(newLevel);
                sendMessageCompat(player, "経験値レベルを " + newLevel + " に変更しました。", GREEN);
            } catch (NumberFormatException e) {
                sendMessageCompat(player, "数値を入力してください。", RED);
            }
            return;
        }

        // --- 通常チャットの表示制御 ---
        String prefix = "";
        try {
            var meta = net.luckperms.api.LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
            prefix = meta.getPrefix() == null ? "" : meta.getPrefix();
        } catch (Throwable ignored) {
            // LuckPerms未導入時はprefix無し
        }

        String nickname = NicknameManager.getDisplayName(player);
        String displayName = prefix + nickname;

        // Paper 1.19+ のみviewers()が使える
        if (APIVersionUtil.isAtLeast(19)) {
            try {
                // 新API: 全参加者にComponentチャット送信
                for (Player viewer : event.getRecipients()) {
                    viewer.sendMessage(Component.text(displayName + " » " + event.getMessage()).color(NamedTextColor.WHITE));
                }
                event.setCancelled(true);
            } catch (Throwable e) {
                // 念のため失敗時も旧式へ
                event.setFormat(displayName + " » " + event.getMessage());
            }
        } else {
            // 旧API（Spigot, 1.17.1等）はsetFormatで一括フォーマット
            event.setFormat(displayName + " » " + event.getMessage());
        }
    }

    // すべてのバージョンで動くsendMessageユーティリティ
    private void sendMessageCompat(Player player, String text, NamedTextColor color) {
        try {
            player.sendMessage(Component.text(text).color(color));
        } catch (Throwable e) {
            player.sendMessage("§" + getLegacyColorCode(color) + text);
        }
    }

    private String getLegacyColorCode(NamedTextColor color) {
        return switch (color) {
            case GREEN -> "a";
            case RED -> "c";
            case AQUA -> "b";
            case YELLOW -> "e";
            case GRAY -> "7";
            case WHITE -> "f";
            default -> "f";
        };
    }
}




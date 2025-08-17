package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.util.LuckPermsUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Chat implements Listener {

    // 待機フラグ
    private static final Map<UUID, Boolean> waitingForNickname = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingForColorInput = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingForExpInput = new ConcurrentHashMap<>();

    public static void setWaitingForNickname(Player player, boolean waiting) {
        if (waiting) waitingForNickname.put(player.getUniqueId(), true);
        else waitingForNickname.remove(player.getUniqueId());
    }
    public static boolean isWaitingForNickname(Player player) {
        return waitingForNickname.getOrDefault(player.getUniqueId(), false);
    }

    public static void setWaitingForColorInput(Player player, boolean waiting) {
        if (waiting) waitingForColorInput.put(player.getUniqueId(), true);
        else waitingForColorInput.remove(player.getUniqueId());
    }
    public static boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.getOrDefault(player.getUniqueId(), false);
    }

    public static void setWaitingForExpInput(Player player, boolean waiting) {
        if (waiting) waitingForExpInput.put(player.getUniqueId(), true);
        else waitingForExpInput.remove(player.getUniqueId());
    }
    public static boolean isWaitingForExpInput(Player player) {
        return waitingForExpInput.getOrDefault(player.getUniqueId(), false);
    }

    /** 旧式(AsyncPlayerChatEvent)専用のハンドラ。Paper新式がある環境では何もしない */
    @EventHandler(ignoreCancelled = true)
    public void onLegacyAsyncChat(AsyncPlayerChatEvent e) {
        // Paperの新式 AsyncChatEvent が存在するなら、ここは処理しない（ChatBridge側に任せる想定）
        if (hasPaperAsyncChat()) return;

        final Player player = e.getPlayer();
        final String msg = e.getMessage();

        // 入力待機（例：経験値）
        if (tryHandleWaitingInput(player, msg)) {
            e.setCancelled(true);
            return;
        }

        // Prefix（LuckPermsが無い環境でも落ちない）
        String prefix = "";
        try { prefix = LuckPermsUtil.safePrefix(player); } catch (Throwable ignored) {}

        // ニックネーム（未設定ならプレイヤー名）
        String nickname = NicknameManager.getDisplayName(player);
        if (nickname == null || nickname.isEmpty()) nickname = player.getName();

        // Prefix二重付与ガード
        if (!prefix.isEmpty() && nickname.startsWith(prefix)) prefix = "";

        String display = prefix.isEmpty()
                ? nickname
                : (prefix + ChatColor.RESET + " " + nickname);

        // setFormat は %1$s(名前) / %2$s(本文) の2引数フォーマット
        // → %1$s は使わず固定文字列にし、%2$s だけ使う（安全）
        e.setFormat(display + ChatColor.RESET + " » %2$s");
    }

    /** 共通の“待機フラグ”処理。消費したら true */
    public static boolean tryHandleWaitingInput(Player player, String plainMessage) {
        if (isWaitingForExpInput(player)) {
            setWaitingForExpInput(player, false);
            try {
                int change = Integer.parseInt(plainMessage.trim());
                int newLevel = Math.max(0, player.getLevel() + change);
                player.setLevel(newLevel);
                player.sendMessage(ChatColor.GREEN + "経験値レベルを " + newLevel + " に変更しました。");
            } catch (NumberFormatException ex) {
                player.sendMessage(ChatColor.RED + "数値を入力してください。");
            }
            return true;
        }
        if (isWaitingForNickname(player)) {
            setWaitingForNickname(player, false);
            if (!plainMessage.trim().isEmpty()) {
                NicknameManager.setNickname(player, plainMessage.trim());
                player.sendMessage(ChatColor.GREEN + "ニックネームを " + plainMessage.trim() + " に設定しました。");
            } else {
                player.sendMessage(ChatColor.RED + "ニックネームは空にできません。");
            }
            return true;
        }

        // どこか Chat クラス内（tryHandleWaitingInput の中）に、あなたの追加分を置き換え
        if (isWaitingForColorInput(player)) {
            // 失敗時は待機を維持して再入力してもらうため、ここでは解除しない
            final String input = plainMessage.trim();

            ChatColor parsed = parseChatColor(input);
            if (parsed != null && parsed.isColor()) {
                // 成功時のみ待機解除＋同期実行で安全に反映
                setWaitingForColorInput(player, false);
                var pl = org.bukkit.Bukkit.getPluginManager().getPlugin("Miniutility");
                if (pl != null) {
                    org.hotamachisubaru.miniutility.util.FoliaUtil.runAtPlayer(pl, player.getUniqueId(), () -> {
                        org.hotamachisubaru.miniutility.Nickname.NicknameManager.setColor(player, parsed);
                        player.sendMessage(org.bukkit.ChatColor.GREEN + "カラーコードを " + parsed.name() + " に設定しました。");
                    });
                } else {
                    // フォールバック（同期保証なし）— ほぼ来ない想定
                    org.hotamachisubaru.miniutility.Nickname.NicknameManager.setColor(player, parsed);
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "カラーコードを " + parsed.name() + " に設定しました。");
                }
            } else {
                // 待機は継続（再入力を促す）
                player.sendMessage(org.bukkit.ChatColor.RED
                        + "無効なカラーコードです。例: RED, BLUE, GREEN / &a, &b, &c / grey=GRAY, pink=LIGHT_PURPLE");
            }
            return true;
        }


        return false;
    }

    // Chat クラスの private メソッドとして追加
    private static ChatColor parseChatColor(String in) {
        if (in == null) return null;
        String s = in.trim();
        if (s.isEmpty()) return null;

        // 1) &a / §c など1文字コード
        if (s.length() >= 2 && (s.charAt(0) == '&' || s.charAt(0) == '§')) {
            ChatColor by = ChatColor.getByChar(Character.toLowerCase(s.charAt(1)));
            return (by != null && by.isColor()) ? by : null;
        }

        // 2) よくあるエイリアス
        if (s.equalsIgnoreCase("grey")) s = "GRAY";
        if (s.equalsIgnoreCase("pink")) s = "LIGHT_PURPLE";
        if (s.equalsIgnoreCase("purple")) s = "DARK_PURPLE";

        // 3) 正式名
        try {
            ChatColor c = ChatColor.valueOf(s.toUpperCase());
            return c.isColor() ? c : null; // 装飾(BOLD 等)は拒否
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }


    /** Paper の AsyncChatEvent 存在チェック（反射） */
    private static boolean hasPaperAsyncChat() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

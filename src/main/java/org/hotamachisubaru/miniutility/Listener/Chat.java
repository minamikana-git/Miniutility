package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.util.FoliaUtil;
import org.hotamachisubaru.miniutility.util.LuckPermsUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getPluginManager;

public class Chat implements Listener {

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

    /**
     * 旧式(AsyncPlayerChatEvent)専用のハンドラ。Paper新式がある環境では何もしない
     */
    @EventHandler(ignoreCancelled = true)
    public void onLegacyAsyncChat(AsyncPlayerChatEvent e) {
        // Paper新式がある環境では二重送信を避ける
        if (hasPaperAsyncChat()) return;

        Player player = e.getPlayer();
        String plain = e.getMessage();

        if (tryHandleWaitingInput(player, plain)) {
            e.setCancelled(true);
            return;
        }

        // ▼ここから通常チャットの整形
        String prefix = "";
        try {
            prefix = LuckPermsUtil.safePrefix(player); // LuckPerms無しでも安全
        } catch (Throwable ignored) {}

        String nickname = NicknameManager.getDisplayName(player);
        if (nickname == null || nickname.isEmpty()) nickname = player.getName();

        // Prefix二重付与ガード
        if (!prefix.isEmpty() && nickname.startsWith(prefix)) prefix = "";

        String display = prefix.isEmpty()
                ? nickname
                : (prefix + ChatColor.RESET + " " + nickname);

        // %1$s(名前)/%2$s(本文)。名前は固定文字列にして本文だけ使う
        e.setFormat(display + ChatColor.RESET + " » %2$s");
    }


    private static boolean hasPaperAsyncChat() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }



    /**
     * 共通の“待機フラグ”処理。消費したら true
     */
    public static boolean tryHandleWaitingInput(Player player, String plainMessage) {

        if (isWaitingForExpInput(player)) {
            setWaitingForExpInput(player, false);
            try {
                int change = Integer.parseInt(plainMessage.trim());
                int newLevel = Math.max(0, player.getLevel() + change);
                player.setLevel(newLevel);
                player.sendMessage(Component.text(  "経験値レベルを " + newLevel + " に変更しました。", NamedTextColor.GREEN));
            } catch (NumberFormatException ex) {
                player.sendMessage(ChatColor.RED + "数値を入力してください。");
            }
            return true;
        }

        if (isWaitingForNickname(player)) {
            final String input = plainMessage == null ? "" : plainMessage.trim();

            String validated = validateNickname(input);
            if (validated != null) {
                setWaitingForNickname(player, false);
                var pl = getPluginManager().getPlugin("Miniutility");
                if (pl != null) {
                    FoliaUtil.runAtPlayer(pl, player.getUniqueId(), () -> {
                        NicknameManager.setNickname(player, validated);
                        player.sendMessage(ChatColor.GREEN + "ニックネームを「" + validated + "」に設定しました。");
                    });
                } else {
                    NicknameManager.setNickname(player, validated);
                    player.sendMessage(Component.text("ニックネームを「" + validated + "」に設定しました。", NamedTextColor.GREEN));
                }
            } else {
                player.sendMessage(Component.text("無効なニックネームです。"
                        + "1〜16文字、記号は _- のみ使用可。空白不可。", NamedTextColor.RED));
            }
            return true;
        }

        setWaitingForColorInput(player, false);

        String raw = plainMessage == null ? "" : plainMessage.trim();
        if (raw.isEmpty()) {
            player.sendMessage(Component.text("例: &6a, &bほたまち", NamedTextColor.RED));
            return true;
        }

        String visible = raw.replaceAll("(?i)[&§][0-9a-fk-or]", "");
        if (validateNickname(visible) == null) {
            player.sendMessage(Component.text("無効なニックネームです。1〜16文字、記号は _- のみ、空白不可。", NamedTextColor.RED));
            return true;
        }

        String colored = ChatColor.translateAlternateColorCodes('&', raw);

        NicknameManager.setNickname(player, colored);
        player.sendMessage(Component.text("ニックネームを設定しました: " + colored, NamedTextColor.GREEN).append(Component.text("").color(null)));
        return true;
    }

    private static ChatColor parseChatColor(String in) {
        if (in == null) return null;
        String s = in.trim();
        if (s.isEmpty()) return null;

        if (s.length() >= 2 && (s.charAt(0) == '&' || s.charAt(0) == '§')) {
            ChatColor by = ChatColor.getByChar(Character.toLowerCase(s.charAt(1)));
            return (by != null && by.isColor()) ? by : null;
        }

        if (s.equalsIgnoreCase("grey")) s = "GRAY";
        if (s.equalsIgnoreCase("pink")) s = "LIGHT_PURPLE";
        if (s.equalsIgnoreCase("purple")) s = "DARK_PURPLE";

        try {
            ChatColor c = ChatColor.valueOf(s.toUpperCase());
            return c.isColor() ? c : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String validateNickname(String s) {
        if (s == null) return null;
        if (s.contains(" ") || s.contains("　")) return null;

        final int len = s.length();
        if (len < 1 || len > 16) return null;

        if (s.matches(".*[<>\"'`$\\\\].*")) return null;

        return s;
    }
}
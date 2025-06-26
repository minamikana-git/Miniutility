package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Chat implements Listener {

    private final Plugin plugin;
    // 入力待機マップ
    private static final Map<UUID, Boolean> waitingForNickname = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingForColorInput = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waitingForExpInput = new ConcurrentHashMap<>();

    // --- Setter/Getter ---
    public static void setWaitingForNickname(Player player, boolean flag) {
        if (flag) waitingForNickname.put(player.getUniqueId(), true);
        else waitingForNickname.remove(player.getUniqueId());
    }
    public static boolean isWaitingForNickname(Player player) {
        return waitingForNickname.containsKey(player.getUniqueId());
    }

    public static void setWaitingForColorInput(Player player, boolean flag) {
        if (flag) waitingForColorInput.put(player.getUniqueId(), true);
        else waitingForColorInput.remove(player.getUniqueId());
    }
    public static boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.containsKey(player.getUniqueId());
    }

    public static void setWaitingForExpInput(Player player, boolean flag) {
        if (flag) waitingForExpInput.put(player.getUniqueId(), true);
        else waitingForExpInput.remove(player.getUniqueId());
    }
    public static boolean isWaitingForExpInput(Player player) {
        return waitingForExpInput.containsKey(player.getUniqueId());
    }




    public Chat(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String msg = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.message());

        // -------------------- ニックネーム入力待機 --------------------
        if (isWaitingForNickname(player)) {
            event.setCancelled(true);
            if (msg.isEmpty()) {
                player.sendMessage(Component.text("ニックネームが空です。もう一度入力してください。", NamedTextColor.RED));
                return;
            }
            if (msg.length() > 16) {
                player.sendMessage(Component.text("ニックネームは16文字以内にしてください。", NamedTextColor.RED));
                setWaitingForNickname(player, false);
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("Miniutility"), () -> {
                NicknameDatabase.saveNickname(uuid.toString(), msg);
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Miniutility"),
                        () -> NicknameManager.applyFormattedDisplayName(player));
            });
            setWaitingForNickname(player, false);
            player.sendMessage(Component.text("✅ ニックネームを「" + msg + "」に設定しました。", NamedTextColor.GREEN));
            return;
        }

        // -------------------- 色コード入力待機 --------------------
        if (isWaitingForColorInput(player)) {
            event.setCancelled(true);
            if (msg.isEmpty() || msg.length() > 16) {
                player.sendMessage(Component.text("無効な入力です。色付き表示したいニックネームを16文字以内で入力してください。", NamedTextColor.RED));
                setWaitingForColorInput(player, false);
                return;
            }
            String translated = msg; // 例: &6ほたまち
            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("Miniutility"), () -> {
                NicknameDatabase.saveNickname(uuid.toString(), translated);
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Miniutility"),
                        () -> NicknameManager.applyFormattedDisplayName(player));
            });
            setWaitingForColorInput(player, false);
            player.sendMessage(Component.text("✅ ニックネームの色を変更しました: ")
                    .append(LegacyComponentSerializer.legacyAmpersand().deserialize(translated))
                    .color(NamedTextColor.GREEN));
            return;
        }

        // -------------------- 経験値入力待機 --------------------
        if (isWaitingForExpInput(player)) {
            event.setCancelled(true);
            try {
                int inputValue = Integer.parseInt(msg);
                if (inputValue >= 0) {
                    player.giveExpLevels(inputValue);
                    player.sendMessage(Component.text("経験値レベルに +" + inputValue + " しました。", NamedTextColor.AQUA));
                } else {
                    int currentLevel = player.getLevel();
                    int target = Math.max(0, currentLevel + inputValue);
                    player.setLevel(target);
                    player.sendMessage(Component.text("経験値レベルから " + (-inputValue) + " 減らしました。", NamedTextColor.RED));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("無効な入力です。整数（例: 10 または -5）を入力してください。", NamedTextColor.RED));
            }
            setWaitingForExpInput(player, false);
            return;
        }

        // -------------------- 通常チャット（独自フォーマット） --------------------
        // Prefix + Nickname + > + 本文
        String prefix = "";
        try {
            prefix = NicknameManager.getLuckPermsPrefix(player);
        } catch (Exception ignored) {}
        String displayNick = NicknameManager.getNickname(player);

        Component chat = Component.empty()
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix))
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(displayNick))
                .append(Component.text(" > ", NamedTextColor.GRAY))
                .append(event.message());

        event.setCancelled(true);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(chat));
    }
}

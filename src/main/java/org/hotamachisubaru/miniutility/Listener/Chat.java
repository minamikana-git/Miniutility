package org.hotamachisubaru.miniutility.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getLogger;

public class Chat implements Listener {
    private final Plugin plugin;
    private static final Logger logger = getLogger();
    private static final Map<UUID, Boolean> waitingForNickname = new HashMap<>();
    private static final Map<UUID, Boolean> waitingForColorInput = new HashMap<>();

    public Chat(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void setWaitingForNickname(Player player, boolean waiting) {
        if (waiting) {
            waitingForNickname.put(player.getUniqueId(), true);
        } else {
            waitingForNickname.remove(player.getUniqueId());
        }
    }

    public static boolean isWaitingForNickname(Player player) {
        return waitingForNickname.getOrDefault(player.getUniqueId(), false);
    }

    public static void setWaitingForColorInput(Player player, boolean waiting) {
        if (waiting) {
            waitingForColorInput.put(player.getUniqueId(), true);
        } else {
            waitingForColorInput.remove(player.getUniqueId());
        }
    }

    public boolean isWaitingForColorInput(Player player) {
        return waitingForColorInput.getOrDefault(player.getUniqueId(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void Nickname(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (isWaitingForNickname(player)) {
            event.setCancelled(true);
            NicknameInput(player, event.originalMessage());
        } else if (isWaitingForColorInput(player)) {
            event.setCancelled(true);
            ColorInput(player, event.originalMessage());
        }
    }


    private void NicknameInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        if (message.isEmpty() || message.length() > 16) { // ニックネームの長さチェック
            player.sendMessage(Component.text("無効なニックネームです。16文字以内の有効なニックネームを入力してください。").color(NamedTextColor.RED));
            setWaitingForNickname(player, false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // ニックネームをデータベースに保存
            NicknameDatabase.saveNickname(player.getUniqueId().toString(), message);

            // 表示名を更新
            updateDisplayNamePrefix(player, message);

            player.sendMessage(Component.text("ニックネームを設定しました: ").color(NamedTextColor.GREEN)
                    .append(Component.text(message).color(NamedTextColor.AQUA)));

            setWaitingForNickname(player, false);
        });
    }

    public void ColorInput(Player player, Component messageComponent) {
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent).trim();

        if (message.isEmpty() || message.length() > 16) { // 入力のバリデーション
            player.sendMessage(Component.text("無効な入力です。有効なカラーコードを16文字以内で指定してください。").color(NamedTextColor.RED));
            setWaitingForColorInput(player, false);
            return;
        }

        String coloredName = ChatColor.translateAlternateColorCodes('&', message);

        if (!coloredName.equals(ChatColor.stripColor(coloredName))) {
            String currentNickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
            if (currentNickname == null || currentNickname.isEmpty()) {
                currentNickname = player.getName();
            }

            String updatedNickname = coloredName;
            NicknameDatabase.saveNickname(player.getUniqueId().toString(), updatedNickname);
            updateDisplayNamePrefix(player, updatedNickname);

            player.sendMessage(Component.text("名前の色を変更しました: ").color(NamedTextColor.GREEN)
                    .append(Component.text(updatedNickname).color(NamedTextColor.AQUA)));
        } else {
            player.sendMessage(Component.text("無効なカラーコードです。例: &6 設定したいニックネーム").color(NamedTextColor.RED));
        }

        setWaitingForColorInput(player, false);
    }

    public static void updateDisplayNamePrefix(Player player, String nickname) {
        CachedMetaData metaData = LuckPermsProvider.get().getPlayerAdapter(Player.class).getMetaData(player);
        String prefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";

        // プレフィックス + ニックネームを正しくカラー適用
        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + nickname);

        // LegacyComponentSerializer を適用
        Component formattedComponent = LegacyComponentSerializer.legacy('&').deserialize(formattedName);

        // プレイヤーの表示名を適用
        player.displayName(formattedComponent);
        player.playerListName(formattedComponent);


    }


}

package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.util.Objects;

public class NicknameListener implements Listener {
    private final NicknameManager nicknameManager;
    private final MiniutilityLoader plugin;

    public NicknameListener(MiniutilityLoader plugin, NicknameManager nicknameManager) {
        this.plugin = plugin;
        this.nicknameManager = nicknameManager;
    }

    @EventHandler
    public void onNicknameMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title;
        try {
            // Component形式にもString形式にも対応
            title = PlainTextComponentSerializer.plainText().serialize(event.getView().title()).trim();
        } catch (Throwable e) {
            title = event.getView().getTitle().trim();
        }

        if (!Objects.equals(title, "ニックネームを変更")) {
            return;
        }
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        switch (item.getType()) {
            case PAPER -> {
                player.sendMessage(Component.text("新しいニックネームをチャットに入力してください。").color(NamedTextColor.AQUA));
                Chat.setWaitingForNickname(player, true);
                player.closeInventory();
            }
            case NAME_TAG -> {
                player.sendMessage(Component.text("色付きのニックネームをチャットで入力してください。例: &6ほたまち").color(NamedTextColor.AQUA));
                Chat.setWaitingForColorInput(player, true);
                player.closeInventory();
            }
            case BARRIER -> {
                NicknameDatabase.deleteNickname(player);
                NicknameManager.applyFormattedDisplayName(player);
                player.sendMessage(Component.text("ニックネームをリセットしました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }
            default -> player.sendMessage(Component.text("無効な選択です。").color(NamedTextColor.RED));
        }
    }

    public static void openNicknameMenu(Player player) {
        // Componentタイトルに対応（Paper全世代）
        Inventory nicknameMenu;
        try {
            nicknameMenu = Bukkit.createInventory(player, 9, Component.text("ニックネームを変更"));
        } catch (Throwable e) {
            // 旧APIはStringタイトル
            nicknameMenu = Bukkit.createInventory(player, 9, "ニックネームを変更");
        }
        nicknameMenu.setItem(2, createMenuItem(Material.PAPER, "ニックネームを入力", "クリックして新しいニックネームを入力"));
        nicknameMenu.setItem(4, createMenuItem(Material.NAME_TAG, "カラーコード指定", "クリックして色付きニックネームを入力"));
        nicknameMenu.setItem(6, createMenuItem(Material.BARRIER, "リセット", "ニックネームをリセット"));
        player.openInventory(nicknameMenu);
    }

    private static ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(NamedTextColor.YELLOW));
            meta.lore(java.util.List.of(Component.text(lore).color(NamedTextColor.GRAY)));
            item.setItemMeta(meta);
        }
        return item;
    }
}

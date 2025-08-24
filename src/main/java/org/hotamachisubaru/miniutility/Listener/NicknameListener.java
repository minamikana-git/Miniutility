package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hotamachisubaru.miniutility.GUI.holder.GuiHolder;
import org.hotamachisubaru.miniutility.GUI.holder.GuiType;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

public class NicknameListener implements Listener {
    private final NicknameManager nicknameManager;
    private final MiniutilityLoader plugin;

    public NicknameListener(MiniutilityLoader plugin, NicknameManager nicknameManager) {
        this.plugin = plugin;
        this.nicknameManager = nicknameManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onNicknameMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        Inventory inv = event.getClickedInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof GuiHolder h)) return;
        if (h.getType() != GuiType.NICKNAME) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        switch (item.getType()) {
            case PAPER -> {
                player.sendMessage(Component.text("新しいニックネームをチャットに入力してください。", NamedTextColor.AQUA));
                Chat.setWaitingForNickname(player, true);
                player.closeInventory();
            }
            case NAME_TAG -> {
                player.sendMessage(Component.text("色付きのニックネームをチャットで入力してください。例: &6ほたまち", NamedTextColor.AQUA));
                Chat.setWaitingForColorInput(player, true);
                player.closeInventory();
            }
            case BARRIER -> {
                NicknameDatabase.deleteNickname(player);
                NicknameManager.updateDisplayName(player);
                player.sendMessage(Component.text("ニックネームをリセットしました。", NamedTextColor.GREEN));
                player.closeInventory();
            }
            default -> player.sendMessage(Component.text("無効な選択です。", NamedTextColor.RED));
        }
    }


    public static void openNicknameMenu(Player player) {
        GuiHolder holder = new GuiHolder(GuiType.NICKNAME, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 9, "ニックネームを変更");
        holder.bind(inv);

        inv.setItem(2, createMenuItem(
                Material.PAPER,
                Component.text("ニックネームを入力", NamedTextColor.YELLOW),
                Component.text("クリックして新しいニックネームを入力", NamedTextColor.GRAY)
        ));
        inv.setItem(4, createMenuItem(
                Material.NAME_TAG,
                Component.text("カラーコード指定", NamedTextColor.YELLOW),
                Component.text("クリックして色付きニックネームを入力", NamedTextColor.GRAY)
        ));
        inv.setItem(6, createMenuItem(
                Material.BARRIER,
                Component.text("リセット", NamedTextColor.YELLOW),
                Component.text("ニックネームをリセット", NamedTextColor.GRAY)
        ));

        player.openInventory(inv);
    }

    private static ItemStack createMenuItem(Material material, Component name, Component lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            java.util.List<Component> loreList = new java.util.ArrayList<>();
            loreList.add(lore);
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}
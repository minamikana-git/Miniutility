package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
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
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof GuiHolder h)) return;
        if (h.getType() != GuiType.NICKNAME) return; // ★ ニックネームGUIのみ処理

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        switch (item.getType()) {
            case PAPER -> {
                player.sendMessage(ChatColor.AQUA + "新しいニックネームをチャットに入力してください。");
                Chat.setWaitingForNickname(player, true);
                player.closeInventory();
            }
            case NAME_TAG -> {
                player.sendMessage(ChatColor.AQUA + "色付きのニックネームをチャットで入力してください。例: &6ほたまち");
                Chat.setWaitingForColorInput(player, true);
                player.closeInventory();
            }
            case BARRIER -> {
                NicknameDatabase.deleteNickname(player);
                NicknameManager.updateDisplayName(player);
                player.sendMessage(ChatColor.GREEN + "ニックネームをリセットしました。");
                player.closeInventory();
            }
            default -> player.sendMessage(ChatColor.RED + "無効な選択です。");
        }
    }

    public static void openNicknameMenu(Player player) {
        GuiHolder holder = new GuiHolder(GuiType.NICKNAME, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 9, "ニックネームを変更");
        holder.bind(inv);

        inv.setItem(2, createMenuItem(
                Material.PAPER,
                ChatColor.YELLOW + "ニックネームを入力",
                ChatColor.GRAY + "クリックして新しいニックネームを入力"
        ));
        inv.setItem(4, createMenuItem(
                Material.NAME_TAG,
                ChatColor.YELLOW + "カラーコード指定",
                ChatColor.GRAY + "クリックして色付きニックネームを入力"
        ));
        inv.setItem(6, createMenuItem(
                Material.BARRIER,
                ChatColor.YELLOW + "リセット",
                ChatColor.GRAY + "ニックネームをリセット"
        ));

        player.openInventory(inv);
    }

    private static ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            java.util.List<String> loreList = new java.util.ArrayList<>();
            loreList.add(lore);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}

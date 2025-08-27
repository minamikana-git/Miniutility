package org.hotamachisubaru.miniutility.Listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
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
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof GuiHolder)) return;
        if (((GuiHolder) holder).getType() != GuiType.NICKNAME) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.PAPER) {
            TextComponent component = new TextComponent();
            component.setText("新しいニックネームをチャットに入力してください。");
            component.setColor(ChatColor.AQUA);

            player.sendMessage(component);
            Chat.setWaitingForNickname(player, true);
            player.closeInventory();
        } else if (item.getType() == Material.NAME_TAG) {
            TextComponent component = new TextComponent();
            component.setText("色付きのニックネームをチャットで入力してください。例: &6ほたまち");
            component.setColor(ChatColor.AQUA);

            player.sendMessage(component);
            Chat.setWaitingForColorInput(player, true);
            player.closeInventory();
        } else if (item.getType() == Material.BARRIER) {
            NicknameDatabase.deleteNickname(player);
            NicknameManager.updateDisplayName(player);

            TextComponent component = new TextComponent();
            component.setText("ニックネームをリセットしました。");
            component.setColor(ChatColor.GREEN);

            player.sendMessage(component);
            player.closeInventory();
        } else {
            TextComponent component = new TextComponent();
            component.setText("無効な選択です。");
            component.setColor(ChatColor.RED);

            player.sendMessage(component);
        }
    }


    public static void openNicknameMenu(Player player) {
        GuiHolder holder = new GuiHolder(GuiType.NICKNAME, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 9, "ニックネームを変更");
        holder.bind(inv);

        inv.setItem(2, createMenuItem(Material.PAPER, "§eニックネームを入力", "§7クリックして新しいニックネームを入力"));
        inv.setItem(4, createMenuItem(Material.NAME_TAG, "§eカラーコード指定", "§7クリックして色付きニックネームを入力"));
        inv.setItem(6, createMenuItem(Material.BARRIER, "§eリセット", "§7ニックネームをリセット"));

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
package org.hotamachisubaru.miniutility.GUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class UtilityGUI {
    public static void openUtilityGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "便利箱");

        // 色付け機能
        ItemStack nameColorIcon = new ItemStack(Material.GREEN_DYE);
        ItemMeta nameColorMeta = nameColorIcon.getItemMeta();
        nameColorMeta.setDisplayName("プレイヤーに色を付ける");
        nameColorIcon.setItemMeta(nameColorMeta);
        gui.setItem(0, nameColorIcon);

        // エンダーチェスト機能
        ItemStack enderChestIcon = new ItemStack(Material.ENDER_CHEST);
        ItemMeta enderMeta = enderChestIcon.getItemMeta();
        enderMeta.setDisplayName("どこでもエンダーチェスト");
        enderChestIcon.setItemMeta(enderMeta);
        gui.setItem(2, enderChestIcon);

        // 作業台機能
        ItemStack craftingTableIcon = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftMeta = craftingTableIcon.getItemMeta();
        craftMeta.setDisplayName("どこでも作業台");
        craftingTableIcon.setItemMeta(craftMeta);
        gui.setItem(4, craftingTableIcon);

        // ゴミ箱機能
        ItemStack trashBoxIcon = new ItemStack(Material.DROPPER);
        ItemMeta trashMeta = trashBoxIcon.getItemMeta();
        trashMeta.setDisplayName("ごみ箱");
        trashBoxIcon.setItemMeta(trashMeta);
        gui.setItem(6, trashBoxIcon);

        // ニックネーム機能
        ItemStack nicknameIcon = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta nicknameMeta = nicknameIcon.getItemMeta();
        nicknameMeta.setDisplayName("ニックネーム変更");
        nicknameIcon.setItemMeta(nicknameMeta);
        gui.setItem(8, nicknameIcon); // ニックネームアイコンを8番スロットに配置

        player.openInventory(gui);
    }

    public static void openUtilityMenu(Player player) {
        Inventory utilityMenu = Bukkit.createInventory(player, 27, Component.text("ユーティリティメニュー"));

        // 色変更アイテム
        utilityMenu.setItem(11, createMenuItem(Material.GREEN_DYE, "色を変更する", "クリックして名前の色を設定"));

        // エンダーチェスト
        utilityMenu.setItem(13, createMenuItem(Material.ENDER_CHEST, "エンダーチェスト", "クリックしてエンダーチェストを開く"));

        // ゴミ箱
        utilityMenu.setItem(15, createMenuItem(Material.DROPPER, "ゴミ箱", "クリックしてゴミ箱を開く"));

        // メニューを開く
        player.openInventory(utilityMenu);
    }

    private static ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
            item.setItemMeta(meta);
        }
        return item;
    }

}

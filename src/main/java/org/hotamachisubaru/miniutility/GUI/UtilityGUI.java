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
        // 便利箱インベントリを作成
        Inventory gui = Bukkit.createInventory(null, 9, Component.text("便利箱"));

        // 各アイテムをセット
        gui.setItem(0, createMenuItem(Material.GREEN_DYE, "プレイヤーに色を付ける", "クリックして名前の色を変更"));
        gui.setItem(2, createMenuItem(Material.ENDER_CHEST, "どこでもエンダーチェスト", "エンダーチェストを開く"));
        gui.setItem(4, createMenuItem(Material.CRAFTING_TABLE, "どこでも作業台", "作業台を開く"));
        gui.setItem(6, createMenuItem(Material.DROPPER, "ごみ箱", "クリックしてごみ箱を開く"));
        gui.setItem(8, createMenuItem(Material.WRITABLE_BOOK, "ニックネーム変更", "クリックしてニックネームを変更"));

        // プレイヤーにインベントリを開かせる
        player.openInventory(gui);
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


    public static void openUtilityMenu(Player player) {
        // ユーティリティメニューを作成
        Inventory utilityMenu = Bukkit.createInventory(player, 27, Component.text("ユーティリティメニュー"));

        // 色変更アイテム
        utilityMenu.setItem(0, createMenuItem(Material.GREEN_DYE, "色を変更する", "クリックして名前の色を設定"));

        // エンダーチェスト
        utilityMenu.setItem(2, createMenuItem(Material.ENDER_CHEST, "エンダーチェスト", "クリックしてエンダーチェストを開く"));

        // ゴミ箱
        utilityMenu.setItem(6, createMenuItem(Material.DROPPER, "ゴミ箱", "クリックしてゴミ箱を開く"));

        // ニックネーム変更アイテム
        utilityMenu.setItem(8, createMenuItem(Material.WRITABLE_BOOK, "ニックネーム変更", "クリックしてニックネームを変更"));

        // 作業台アイテム
        utilityMenu.setItem(4, createMenuItem(Material.CRAFTING_TABLE, "どこでも作業台", "クリックして作業台を開く"));

        // プレイヤーにメニューを開かせる
        player.openInventory(utilityMenu);
    }

    /**
     * メニューアイテムを作成するヘルパーメソッド
     *
     * @param material アイテムの素材
     * @param name     アイテムの名前
     * @param lore     アイテムの説明
     * @return 作成されたアイテム
     */


}

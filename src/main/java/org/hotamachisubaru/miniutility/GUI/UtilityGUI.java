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

    public static void openUtilityMenu(Player player) {
        // ユーティリティメニューを作成
        Inventory utilityMenu = Bukkit.createInventory(player, 27, Component.text("便利箱"));

        //クリーパーのブロック破壊を防ぐ
        utilityMenu.setItem(18,createMenuItem(Material.CREEPER_HEAD,"クリーパーのブロック破壊を防ぐ","クリーパーのブロック破壊を防ぎます。ON/OFFができます。"));

        // エンダーチェスト
        utilityMenu.setItem(20, createMenuItem(Material.ENDER_CHEST, "エンダーチェスト", "クリックしてエンダーチェストを開く"));

        // ゴミ箱
        utilityMenu.setItem(22, createMenuItem(Material.DROPPER, "ゴミ箱", "クリックしてゴミ箱を開く"));

        // ニックネーム変更アイテム
        utilityMenu.setItem(24, createMenuItem(Material.WRITABLE_BOOK, "ニックネーム変更", "クリックしてニックネームを変更"));

        // 作業台アイテム
        utilityMenu.setItem(26, createMenuItem(Material.CRAFTING_TABLE, "どこでも作業台", "クリックして作業台を開く"));

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
    private static ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name); // アイテム名を黄色に設定
            meta.setLore(Collections.singletonList(ChatColor.GRAY + lore)); // アイテム説明を灰色に設定
            item.setItemMeta(meta);
        }
        return item;
    }
}

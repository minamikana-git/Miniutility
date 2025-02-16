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
        Inventory utilityMenu = Bukkit.createInventory(player, 27, Component.text("メニュー"));

        //クリーパーのブロック破壊を防ぐ
        utilityMenu.setItem(9, createMenuItem(Material.CREEPER_HEAD, "クリーパーのブロック破壊を防ぐ", "クリーパーのブロック破壊を防ぎます。ON/OFFができます。"));

        // エンダーチェスト
        utilityMenu.setItem(11, createMenuItem(Material.ENDER_CHEST, "エンダーチェスト", "クリックしてエンダーチェストを開く"));

        // ゴミ箱
        utilityMenu.setItem(13, createMenuItem(Material.DROPPER, "ゴミ箱", "クリックしてゴミ箱を開く"));

        // ニックネーム変更アイテム
        utilityMenu.setItem(15, createMenuItem(Material.NAME_TAG, "ニックネーム変更", "クリックしてニックネームを変更"));

        // 作業台アイテム
        utilityMenu.setItem(17, createMenuItem(Material.CRAFTING_TABLE, "どこでも作業台", "クリックして作業台を開く"));

        // プレイヤーにメニューを開かせる
        player.openInventory(utilityMenu);
    }

    public static void openNicknameMenu(Player player){
        Inventory nicknameMenu =  Bukkit.createInventory(player,9,Component.text("ニックネーム"));

        nicknameMenu.setItem(3,createMenuItem(
                Material.PAPER,
                "ニックネームを変更",
                "クリックして新しいニックネームを入力"
        ));

        nicknameMenu.setItem(5,createMenuItem(
                Material.BARRIER,
                "ニックネームをリセット",
                "クリックしてニックネームをリセット"
        ));

        player.openInventory(nicknameMenu);
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
            meta.displayName(Component.text(ChatColor.YELLOW + name)); // アイテム名を黄色に設定
            meta.lore(Collections.singletonList(Component.text(ChatColor.GRAY + lore))); // アイテム説明を灰色に設定
            item.setItemMeta(meta);
        }
        return item;
    }
}

package org.hotamachisubaru.miniutility.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

import java.util.Collections;

public class GUI {

    // ユーティリティメニューを開く
    public static void openMenu(Player player, MiniutilityLoader plugin) {

        Inventory utilityMenu = Bukkit.createInventory(player, 27, Component.text("メニュー"));
        // 死亡地点にワープ
        utilityMenu.setItem(0, createMenuItem(Material.ARMOR_STAND, "死亡地点にワープ", "死亡地点にワープします。溺れたり、溶岩遊泳した場合は安全な場所にテレポートします。"));
        // 経験値制御器
        utilityMenu.setItem(2, createMenuItem(Material.EXPERIENCE_BOTTLE, "経験値制御器", "経験値を制御します"));
        // ゲームモード制御器
        utilityMenu.setItem(4, createMenuItem(Material.COMPASS, "ゲームモード制御器", "ゲームモードを制御します"));
        // 2段ジャンプ
        utilityMenu.setItem(6,createMenuItem(Material.FEATHER,"２段ジャンプ","２段ジャンプを有効/無効します。"));
        // クリーパーのブロック破壊を防ぐ
        utilityMenu.setItem(9, createMenuItem(Material.CREEPER_HEAD, "クリーパーのブロック破壊を防ぐ", "クリーパーのブロック破壊を防ぎます。ON/OFFができます。"));
        // エンダーチェスト
        utilityMenu.setItem(11, createMenuItem(Material.ENDER_CHEST, "エンダーチェスト", "クリックしてエンダーチェストを開く"));
        // ゴミ箱
        utilityMenu.setItem(13, createMenuItem(Material.DROPPER, "ゴミ箱", "クリックしてゴミ箱を開く"));
        // ニックネーム変更アイテム
        utilityMenu.setItem(15, createMenuItem(Material.NAME_TAG, "ニックネームを変更", "クリックしてニックネームを変更"));
        // 作業台アイテム
        utilityMenu.setItem(17, createMenuItem(Material.CRAFTING_TABLE, "どこでも作業台", "クリックして作業台を開く"));


        player.openInventory(utilityMenu);
    }

    // ニックネーム変更メニュー
    public static void NicknameMenu(Player player) {
        Inventory nicknameMenu = Bukkit.createInventory(player, 9, Component.text("ニックネームを変更"));

        nicknameMenu.setItem(2, createMenuItem(
                Material.PAPER,
                "ニックネーム入力",
                "クリックして新しいニックネームを入力"
        ));

        nicknameMenu.setItem(4, createMenuItem(
                Material.NAME_TAG,
                "カラーコード指定",
                "クリックして色付きニックネームを入力"
        ));

        nicknameMenu.setItem(6, createMenuItem(
                Material.BARRIER,
                "リセット",
                "ニックネームをリセット"
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
    public static ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // `&` 記法を使って色を適用
            meta.displayName(LegacyComponentSerializer.legacy('&').deserialize("&e" + name));
            meta.lore(Collections.singletonList(LegacyComponentSerializer.legacy('&').deserialize("&7" + lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    // オーバーロード（lore無し版）も用意
    public static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacy('&').deserialize("&e" + name));
            item.setItemMeta(meta);
        }
        return item;
    }
}

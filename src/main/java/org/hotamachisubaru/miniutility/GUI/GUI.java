package org.hotamachisubaru.miniutility.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hotamachisubaru.miniutility.GUI.holder.GuiHolder;
import org.hotamachisubaru.miniutility.GUI.holder.GuiType;

import java.util.Collections;

public class GUI {

    // ユーティリティメニューを開く（Holder判定・文字列タイトルで統一）
    public static void openMenu(Player player) {
        GuiHolder holder = new GuiHolder(GuiType.MENU, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 27, "メニュー"); // ★ holder を必ず使う
        holder.bind(inv);

        inv.setItem(0,  createMenuItem(Material.ARMOR_STAND,   "死亡地点にワープ",   "死亡地点にワープします。溺れたり、溶岩遊泳した場合は安全な場所にテレポートします。"));
        inv.setItem(2,  createMenuItem(Material.EXPERIENCE_BOTTLE, "経験値制御器",   "経験値を制御します"));
        inv.setItem(4,  createMenuItem(Material.COMPASS,       "ゲームモード制御器", "ゲームモードを制御します"));
        inv.setItem(9,  createMenuItem(Material.CREEPER_HEAD,  "クリーパーのブロック破壊を防ぐ", "クリーパーのブロック破壊を防ぎます。ON/OFFができます。"));
        inv.setItem(11, createMenuItem(Material.ENDER_CHEST,   "エンダーチェスト", "クリックしてエンダーチェストを開く"));
        inv.setItem(13, createMenuItem(Material.DROPPER,       "ゴミ箱",           "クリックしてゴミ箱を開く"));
        inv.setItem(15, createMenuItem(Material.NAME_TAG,      "ニックネームを変更", "クリックしてニックネームを変更"));
        inv.setItem(17, createMenuItem(Material.CRAFTING_TABLE,"どこでも作業台",   "クリックして作業台を開く"));

        player.openInventory(inv);
    }


    // ニックネーム変更メニュー
    public static void NicknameMenu(Player player) {
        GuiHolder holder = new GuiHolder(GuiType.NICKNAME,player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder,9,"ニックネームを変更");
        holder.bind(inv);
        try {
            inv = Bukkit.createInventory(player, 9, Component.text("ニックネームを変更"));
        } catch (Throwable e) {
           inv = Bukkit.createInventory(player, 9, "ニックネームを変更");
        }

        inv.setItem(2, createMenuItem(
                Material.PAPER,
                "ニックネーム入力",
                "クリックして新しいニックネームを入力"
        ));

        inv.setItem(4, createMenuItem(
                Material.NAME_TAG,
                "カラーコード指定",
                "クリックして色付きニックネームを入力"
        ));

        inv.setItem(6, createMenuItem(
                Material.BARRIER,
                "リセット",
                "ニックネームをリセット"
        ));

        player.openInventory(inv);
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

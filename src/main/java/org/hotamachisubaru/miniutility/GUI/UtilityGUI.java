package org.hotamachisubaru.miniutility.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class UtilityGUI {

    public static void openUtilityMenu(Player player) {
        // ユーティリティメニューを作成
        Inventory utilityMenu = Bukkit.createInventory(player, 27, Component.text("メニュー"));

        //死亡地点にワープ
        utilityMenu.setItem(0,createMenuItem(Material.ARMOR_STAND,"死亡地点にワープ","死亡地点にワープします。溺れたり、溶岩遊泳した場合は安全な場所にテレポートします。"));
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
        Inventory nicknameMenu =  Bukkit.createInventory(player,9,Component.text("ニックネーム変更"));

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
            // `&` 記法を使って色を適用
            meta.displayName(LegacyComponentSerializer.legacy('&').deserialize("&e" + name));
            meta.lore(Collections.singletonList(LegacyComponentSerializer.legacy('&').deserialize("&7" + lore)));

            item.setItemMeta(meta);
        }
        return item;
    }


    @EventHandler
    public static void openTrashBox(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inventory = event.getInventory();

        // ゴミ箱インベントリを識別
        if (inventory.getHolder() != null && inventory.getHolder().equals(player)
                && event.getView().title().equals(Component.text("ゴミ箱"))) {

            // 確認ボタン（スロット53）が押された場合
            if (event.getSlot() == 53) {
                event.setCancelled(true); // クリック動作をキャンセル
                openTrashConfirm(player); // 確認メニューを開く
            }
        }
    }

    public static void openTrashConfirm(Player player) {
        Inventory confirmMenu = Bukkit.createInventory(player, 9, Component.text("本当に捨てますか？"));

        confirmMenu.setItem(3, createMenuItem(
                Material.LIME_CONCRETE,
                "はい",
                "クリックしてゴミ箱を空にする"
        ));

        confirmMenu.setItem(5, createMenuItem(
                Material.RED_CONCRETE,
                "いいえ",
                "クリックしてキャンセル"
        ));

        player.openInventory(confirmMenu);
    }


}

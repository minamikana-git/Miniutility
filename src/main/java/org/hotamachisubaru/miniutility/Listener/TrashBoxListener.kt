package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class TrashBoxListener implements Listener {

    private Inventory lastTrashInventory;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // 便利箱のインベントリが開いている場合
        if (event.getView().getTitle().equals("便利箱")) {
            event.setCancelled(true);  // 便利箱内の操作をキャンセル

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            if (clickedItem.getType() == Material.DROPPER) {
                // ゴミ箱を開く処理
                Inventory trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN + "ゴミ箱");

                // 確認ボタンを右下に配置
                ItemStack confirmButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta confirmMeta = confirmButton.getItemMeta();
                confirmMeta.setDisplayName(ChatColor.GREEN + "捨てる");
                confirmButton.setItemMeta(confirmMeta);

                trashInventory.setItem(53, confirmButton);

                // ゴミ箱のインベントリをlastTrashInventoryに保存
                lastTrashInventory = trashInventory;

                player.openInventory(trashInventory);
            }
        }

        // ゴミ箱インベントリを開いている場合
        if (event.getView().getTitle().equals(ChatColor.GREEN + "ゴミ箱")) {
            if (event.getRawSlot() == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
                // スロット53（「捨てる」ボタン）のクリックをキャンセル
                event.setCancelled(true);

                // 確認画面を開く
                Inventory confirmInventory = Bukkit.createInventory(player, 27, ChatColor.RED + "本当に捨てますか？");

                // Yesボタン (緑色のガラス)
                ItemStack yesItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta yesMeta = yesItem.getItemMeta();
                yesMeta.setDisplayName(ChatColor.GREEN + "はい");
                yesItem.setItemMeta(yesMeta);

                // Noボタン (赤色のガラス)
                ItemStack noItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta noMeta = noItem.getItemMeta();
                noMeta.setDisplayName(ChatColor.RED + "いいえ");
                noItem.setItemMeta(noMeta);

                confirmInventory.setItem(11, yesItem);
                confirmInventory.setItem(15, noItem);

                player.openInventory(confirmInventory);
            } else {
                // ゴミ箱内でのアイテム移動を許可
                event.setCancelled(false);
            }
        }

        // 確認画面が開いている場合
        if (event.getView().getTitle().equals(ChatColor.RED + "本当に捨てますか？")) {
            event.setCancelled(true);  // 確認画面内での操作をキャンセル

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                // プレイヤーのインベントリをクリア（ゴミ箱にあるアイテムを削除）
                if (lastTrashInventory != null) {
                    lastTrashInventory.clear();  // アイテムを削除
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "アイテムを削除しました。");
                } else {
                    player.sendMessage(ChatColor.RED + "エラー: ゴミ箱のインベントリが見つかりません。");
                }
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                // キャンセル（ゴミ箱のアイテムを戻す）
                player.closeInventory();
                if (lastTrashInventory != null) {
                    for (ItemStack item : lastTrashInventory.getContents()) {
                        if (item != null) {
                            player.getInventory().addItem(item);  // プレイヤーにアイテムを返す
                        }
                    }
                    player.sendMessage(ChatColor.YELLOW + "アイテムの削除をキャンセルしました。");
                } else {
                    player.sendMessage(ChatColor.RED + "エラー: ゴミ箱のインベントリが見つかりません。");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (event.getView().getTitle().equals(ChatColor.RED + "ゴミ箱") && inventory != null) {
            // ゴミ箱の内容がある場合にのみクリア
            if (inventory.getSize() > 0) {
                inventory.clear();  // ゴミ箱の内容をクリア
            }
        }
    }
}

package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrashBox implements Listener {
    // プレイヤーごとのゴミ箱インベントリを保持
    private final Map<UUID, Inventory> lastTrashInventories = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // 便利箱のインベントリが開いている場合
        if ("便利箱".equals(event.getView().getTitle())) {
            event.setCancelled(true); // 便利箱内の操作をキャンセル

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            if (clickedItem.getType() == Material.DROPPER) {
                // ゴミ箱を開く処理
                Inventory trashInventory = Bukkit.createInventory(player, 54, ChatColor.GREEN + "ゴミ箱");

                // 確認ボタンを右下に配置
                ItemStack confirmButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                confirmButton.getItemMeta().setDisplayName(ChatColor.GREEN + "捨てる");
                trashInventory.setItem(53, confirmButton);

                // ゴミ箱のインベントリをプレイヤーごとに保存
                lastTrashInventories.put(player.getUniqueId(), trashInventory);

                player.openInventory(trashInventory);
            }
        }

        // ゴミ箱インベントリが開いている場合
        if ((ChatColor.GREEN + "ゴミ箱").equals(event.getView().getTitle())) {
            if (event.getRawSlot() == 53 && clickedItem != null && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                // 「捨てる」ボタンのクリックをキャンセル
                event.setCancelled(true);

                // 確認画面を開く
                Inventory confirmInventory = Bukkit.createInventory(player, 27, ChatColor.RED + "本当に捨てますか？");

                // Yesボタン (緑色のガラス)
                ItemStack yesItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                yesItem.getItemMeta().setDisplayName(ChatColor.GREEN + "はい");
                confirmInventory.setItem(11, yesItem);

                // Noボタン (赤色のガラス)
                ItemStack noItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                noItem.getItemMeta().setDisplayName(ChatColor.RED + "いいえ");
                confirmInventory.setItem(15, noItem);

                player.openInventory(confirmInventory);
            } else {
                // ゴミ箱内でのアイテム移動を許可
                event.setCancelled(false);
            }
        }

        // 確認画面が開いている場合
        if ((ChatColor.RED + "本当に捨てますか？").equals(event.getView().getTitle())) {
            event.setCancelled(true); // 確認画面内での操作をキャンセル

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                // プレイヤーのインベントリをクリア（ゴミ箱にあるアイテムを削除）
                Inventory trashInventory = lastTrashInventories.get(player.getUniqueId());
                if (trashInventory != null) {
                    trashInventory.clear(); // アイテムを削除
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "アイテムを削除しました。");
                } else {
                    player.sendMessage(ChatColor.RED + "エラー: ゴミ箱のインベントリが見つかりません。");
                }
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                // キャンセル（ゴミ箱のアイテムを戻す）
                player.closeInventory();
                Inventory trashInventory = lastTrashInventories.get(player.getUniqueId());
                if (trashInventory != null) {
                    for (ItemStack item : trashInventory.getContents()) {
                        if (item != null) {
                            player.getInventory().addItem(item); // プレイヤーにアイテムを返す
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
        if ((ChatColor.GREEN + "ゴミ箱").equals(event.getView().getTitle())) {
            Inventory inventory = event.getInventory();
            if (inventory != null) {
                inventory.clear(); // ゴミ箱の内容をクリア
            }
        }
    }
}
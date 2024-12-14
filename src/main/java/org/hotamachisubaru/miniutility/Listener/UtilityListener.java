package org.hotamachisubaru.miniutility.Listener;



import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class UtilityListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // インベントリタイトルを取得
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        switch (title) {
            case "便利箱" -> handleUtilityBoxClick(player, clickedItem, event);
            case "ゴミ箱" -> handleTrashBoxClick(player, clickedItem, event);
            case "本当に捨てますか？" -> handleTrashConfirmClick(player, clickedItem, event);
        }
    }

    private void handleUtilityBoxClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true); // アイテムの移動を防ぐ
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // アイテムに応じた動作
        switch (clickedItem.getType()) {
            case ENDER_CHEST -> player.openInventory(player.getEnderChest());
            case CRAFTING_TABLE -> player.openWorkbench(null, true);
            case DROPPER -> openTrashBox(player);
            default -> player.sendMessage(ChatColor.RED + "このアイテムにはアクションが設定されていません。");
        }
    }

    private void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        ItemStack confirmButton = createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "捨てる");
        trashInventory.setItem(53, confirmButton);
        player.openInventory(trashInventory);
    }

    private void handleTrashBoxClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true); // アイテム移動を防ぐ

        // ゴミ箱にアイテムを入れる場合はキャンセルしない
        if (event.getRawSlot() < event.getClickedInventory().getSize()) {
            if (clickedItem != null && clickedItem.getType() != Material.GREEN_STAINED_GLASS_PANE) {
                return; // ゴミ箱に移動を許可
            }
        }

        // 確認ボタンのクリック処理
        if (event.getRawSlot() == 53 && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            openTrashConfirm(player);
        }
    }

    private void openTrashConfirm(Player player) {
        Inventory confirmInventory = Bukkit.createInventory(player, 27, Component.text("本当に捨てますか？"));
        confirmInventory.setItem(11, createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "はい"));
        confirmInventory.setItem(15, createMenuItem(Material.RED_STAINED_GLASS_PANE, "いいえ"));
        player.openInventory(confirmInventory);
    }

    private void handleTrashConfirmClick(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true); // アイテム移動を防ぐ
        if (clickedItem == null) return;

        switch (clickedItem.getType()) {
            case GREEN_STAINED_GLASS_PANE -> {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "アイテムを削除しました。");
            }
            case RED_STAINED_GLASS_PANE -> {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "削除をキャンセルしました。");
            }
            default -> player.sendMessage(ChatColor.RED + "無効な選択です。");
        }
    }

    private ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(ChatColor.YELLOW + name));
            item.setItemMeta(meta);
        }
        return item;
    }
}

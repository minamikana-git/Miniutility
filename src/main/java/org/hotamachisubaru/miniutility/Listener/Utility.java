package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;
import org.hotamachisubaru.miniutility.Miniutility;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

public class Utility implements Listener {

    private boolean creeperProtectionEnabled = false;
    
    @EventHandler
    public void Utility(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // インベントリタイトルを取得
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        switch (title) {
            case "メニュー" -> handleUtilityBox(player, clickedItem, event);
            case "ゴミ箱" -> handleTrashBox(player, clickedItem, event);
            case "本当に捨てますか？" -> TrashConfirm(player, clickedItem, event);
        }
    }

    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true); // アイテムの移動を防ぐ
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");

        // アイテムに応じた動作
        switch (clickedItem.getType()) {
            case ENDER_CHEST -> player.openInventory(player.getEnderChest());
            case CRAFTING_TABLE -> player.openWorkbench(null, true);
            case DROPPER -> openTrashBox(player);
            case NAME_TAG -> {
               UtilityGUI.openNicknameMenu(player);
            }
            case CREEPER_HEAD -> {
                creeperProtectionEnabled = !creeperProtectionEnabled; // 切り替え
                String status = creeperProtectionEnabled ? "有効" : "無効";
                player.sendMessage(ChatColor.GREEN + "クリーパーの爆破によるブロック破壊防止が " + status + " になりました。");
                player.closeInventory();
            }
            default -> player.sendMessage(ChatColor.RED + "このアイテムにはアクションが設定されていません。");
        }
    }

    private void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        ItemStack confirmButton = createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "捨てる");
        trashInventory.setItem(53, confirmButton);
        player.openInventory(trashInventory);
    }

    private void handleTrashBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        if (event.getRawSlot() < event.getClickedInventory().getSize()) {
            if (clickedItem != null && clickedItem.getType() != Material.GREEN_STAINED_GLASS_PANE) {
                return; // ゴミ箱に移動を許可
            }
        }

        if (event.getRawSlot() == 53 && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            Confirm(player);
        }
    }

    private void Confirm(Player player) {
        Inventory confirmInventory = Bukkit.createInventory(player, 27, Component.text("本当に捨てますか？"));
        confirmInventory.setItem(11, createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "はい"));
        confirmInventory.setItem(15, createMenuItem(Material.RED_STAINED_GLASS_PANE, "いいえ"));
        player.openInventory(confirmInventory);
    }

    private void TrashConfirm(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);
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

    @EventHandler
    public void CreeperExplodeCancel(EntityExplodeEvent event) {
        if (creeperProtectionEnabled && event.getEntity() instanceof Creeper) {
            event.blockList().clear(); // クリーパー爆発のブロック破壊のみを防ぐ
        }
    }

    @EventHandler
    public void setNickname(InventoryClickEvent event) {
        if (event.getView().title().equals(Component.text("ニックネーム"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            switch (clickedItem.getType()) {
                case PAPER -> {
                    // ニックネームを変更
                    player.sendMessage(ChatColor.YELLOW + "新しいニックネームをチャットに入力してください。");
                    // チャット入力待機状態にする
                    Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");
                    Chat.setWaitingForNickname(player, true);
                    player.closeInventory();
                }
                case BARRIER -> {
                    // ニックネームをリセット
                    NicknameDatabase.saveNickname(player.getUniqueId().toString(), "");
                    NicknameManager.applyFormattedDisplayName(player);
                    player.sendMessage(ChatColor.GREEN + "ニックネームをリセットしました。");
                    player.closeInventory();
                }
                default -> {
                    // それ以外は何もしない

                }
            }
        }
    }

}
        

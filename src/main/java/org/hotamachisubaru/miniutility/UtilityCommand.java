package org.hotamachisubaru.miniutility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;

public class UtilityCommand implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // コマンドが "/mu" だった場合
            if (label.equalsIgnoreCase("menu")) {
                // ユーティリティGUIを開く
                UtilityGUI.openUtilityGUI(player);
                return true;
            }
        } else {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます。");
        }

        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // GUI のタイトルが「便利箱」の場合のみ処理
        if (event.getView().getTitle().equals("便利箱")) {
            event.setCancelled(true);  // クリック時にインベントリを操作できないようにする

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;  // 空のスロットをクリックした場合は何もしない
            }

            // どこでもかまどのアイコンがクリックされた場合
            if (clickedItem.getType() == Material.FURNACE) {
                // 新しいかまどインベントリを作成して開く
                Inventory furnaceInventory = Bukkit.createInventory(null, InventoryType.FURNACE, "かまど");
                player.openInventory(furnaceInventory);
                player.closeInventory();  // 便利箱を閉じる
            }
        }
    }
}

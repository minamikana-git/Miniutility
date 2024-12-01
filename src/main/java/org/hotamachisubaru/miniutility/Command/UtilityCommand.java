package org.hotamachisubaru.miniutility.Command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.hotamachisubaru.miniutility.GUI.Utility;

public class UtilityCommand implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            // コマンドが "/menu" だった場合
            if (label.equalsIgnoreCase("menu")) {
                // ユーティリティGUIを開く
                Utility.openUtilityGUI((Player) sender);
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
        if (event.getView().getTitle().equals("便利箱")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

        }
    }
}

package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;

public class UtilityCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみが使用できます。").color(NamedTextColor.RED));
            return true;
        }

        // メニューを開く
        UtilityGUI.openUtilityMenu(player);
        return true;
    }


    @EventHandler
    public void openMenu(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().title().equals(Component.text("メニュー"))) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            }
        }
    }
}

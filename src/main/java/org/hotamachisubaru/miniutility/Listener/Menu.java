package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;

public class Menu implements Listener {

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (!"メニュー".equals(title)) return;

        event.setCancelled(true); // **クリックイベントをキャンセルし、2回処理されるのを防ぐ**

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem.getType().isAir()) return;

        switch (clickedItem.getType()) {
            case ARMOR_STAND -> {
                if (!player.hasMetadata("death_teleported")) {
                    teleportToDeathLocation(player);
                    player.setMetadata("death_teleported", new org.bukkit.metadata.FixedMetadataValue(Bukkit.getPluginManager().getPlugin("Miniutility"), true));
                }
            }

            case ENDER_CHEST -> player.openInventory(player.getEnderChest());
            case CRAFTING_TABLE -> player.openWorkbench(null, true);
            case DROPPER -> UtilityGUI.openTrashBox(event);
            case NAME_TAG -> UtilityGUI.openNicknameMenu(player);
            default ->
                    player.sendMessage(Component.text("このアイテムにはアクションが設定されていません。").color(NamedTextColor.RED));
        }
    }


    private void teleportToDeathLocation(Player player) {
        if (player.getLastDeathLocation() != null) {
            player.teleportAsync(player.getLastDeathLocation());
        } else {
            player.sendMessage(Component.text("死亡地点が見つかりません。").color(NamedTextColor.RED));
        }
    }
}



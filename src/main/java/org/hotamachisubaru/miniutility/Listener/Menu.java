package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
            case SMITHING_TABLE -> {
                // 最寄り鍛冶台探索
                Location found = findNearestSmithingTable(player.getLocation(), 100); // 半径100ブロック探索
                if (found != null) {
                    player.sendMessage(Component.text("最寄りの鍛冶台: ")
                            .append(Component.text("X: " + found.getBlockX() + ", Y: " + found.getBlockY() + ", Z: " + found.getBlockZ()).color(NamedTextColor.AQUA)));
                } else {
                    player.sendMessage(Component.text("近くに鍛冶台は見つかりませんでした。").color(NamedTextColor.RED));
                }
            }

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

    private Location findNearestSmithingTable(Location center, int radius) {
        World world = center.getWorld();
        double minDist = Double.MAX_VALUE;
        Location nearest = null;

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(0, cy - 20); y <= Math.min(world.getMaxHeight(), cy + 20); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    if (world.getBlockAt(x, y, z).getType() == Material.SMITHING_TABLE) {
                        double dist = center.distanceSquared(new Location(world, x, y, z));
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = new Location(world, x, y, z);
                        }
                    }
                }
            }
        }
        return nearest;
    }

}



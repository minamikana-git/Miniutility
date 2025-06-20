package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;
import org.hotamachisubaru.miniutility.Miniutility;

public class Menu implements Listener {

    private final PluginManager pm = Bukkit.getPluginManager();
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
            case ARMOR_STAND:
                // 死亡地点にワープ
                if (!player.hasMetadata("death_teleported")) {
                    teleportToDeathLocation(player);
                    player.setMetadata("death_teleported", new org.bukkit.metadata.FixedMetadataValue(Bukkit.getPluginManager().getPlugin("Miniutility"), true));
                }
                break;

            case EXPERIENCE_BOTTLE: {
                player.closeInventory();
                Chat.setWaitingForExpInput(player, true);
                player.sendMessage(
                        Component.text("経験値を増減する数値をチャットに入力してください。").color(NamedTextColor.AQUA)
                                .append(Component.text(" 例: \"10\" で +10レベル, \"-5\" で -5レベル").color(NamedTextColor.GRAY))
                );
                break;
            }

            case COMPASS: {
                GameMode current = player.getGameMode();
                if (current == GameMode.SURVIVAL) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(Component.text("ゲームモードをクリエイティブに変更しました。").color(NamedTextColor.GREEN));
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(Component.text("ゲームモードをサバイバルに変更しました。").color(NamedTextColor.GREEN));
                }
                player.closeInventory();
                break;
            }


            case SMITHING_TABLE:
                // 鍛冶台座標検索
                Location found = findNearestSmithingTable(player.getLocation(), 100);
                if (found != null) {
                    player.sendMessage(Component.text("最寄りの鍛冶台: ")
                            .append(Component.text("X: " + found.getBlockX() + ", Y: " + found.getBlockY() + ", Z: " + found.getBlockZ()).color(NamedTextColor.AQUA)));
                } else {
                    player.sendMessage(Component.text("近くに鍛冶台は見つかりませんでした。").color(NamedTextColor.RED));
                }
                break;

            case ENDER_CHEST:
                // エンダーチェスト
                player.openInventory(player.getEnderChest());
                break;

            case DROPPER:
                // ゴミ箱
                player.closeInventory();
                UtilityGUI.openTrashBox(player);
                break;

            case NAME_TAG:
                // ニックネーム変更
                UtilityGUI.openNicknameMenu(player);
                break;

            case CRAFTING_TABLE:
                // 作業台
                player.openWorkbench(null, true);
                break;

            default:
                player.sendMessage(Component.text("このアイテムにはアクションが設定されていません。").color(NamedTextColor.RED));
                break;
        }

    }


    private void teleportToDeathLocation(Player player) {
        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");
        if (plugin == null) {
            player.sendMessage(Component.text("プラグインが読み込まれていません。").color(NamedTextColor.RED));
            return;
        }
        Location loc = plugin.getDeathLocation(player.getUniqueId());
        if (loc == null) {
            player.sendMessage(Component.text("死亡地点が見つかりません。").color(NamedTextColor.RED));
            return;
        }
        player.teleportAsync(loc);
        player.sendMessage(Component.text("死亡地点にワープしました。").color(NamedTextColor.GREEN));
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



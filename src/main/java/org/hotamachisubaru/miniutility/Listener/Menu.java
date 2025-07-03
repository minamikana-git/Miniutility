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
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.Miniutility;

public class Menu implements Listener {

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title()).trim();

        // ★ Miniutilityが管理するGUIだけをswitchで分岐
        switch (title) {
            case "メニュー" -> {
                event.setCancelled(true);
                handleUtilityBox(player, event.getCurrentItem());
            }
            case "ゴミ箱" -> {
                event.setCancelled(true);
                GUI.TrashBox(player);
            }
            case "本当に捨てますか？" -> {
                event.setCancelled(true);
                GUI.TrashConfirm(player);
            }
            case "ニックネームを変更" -> {
                event.setCancelled(true);
                GUI.NicknameMenu(player);
            }
            default -> {
                // それ以外は絶対何もしない
            }
        }
    }

    // --- メニューGUIのクリックアクション処理 ---
    private void handleUtilityBox(Player player, ItemStack clickedItem) {
        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");

        switch (clickedItem.getType()) {
            case ARMOR_STAND -> {
                // 死亡地点ワープ
                teleportToDeathLocation(player);
            }
            case ENDER_CHEST -> {
                player.openInventory(player.getEnderChest());
            }
            case CRAFTING_TABLE -> {
                player.openWorkbench(null, true);
            }
            case DROPPER -> {
                GUI.TrashBox(player);
            }
            case NAME_TAG -> {
                GUI.NicknameMenu(player);
            }
            case CREEPER_HEAD -> {
                CreeperProtectionListener creeperProtection = plugin.getCreeperProtectionListener();
                boolean enabled = creeperProtection.toggleCreeperProtection();
                String status = enabled ? "有効" : "無効";
                player.sendMessage(Component.text("クリーパーの爆破によるブロック破壊防止が " + status + " になりました。", NamedTextColor.GREEN));
                player.closeInventory();
            }
            case EXPERIENCE_BOTTLE -> {
                player.closeInventory();
                Chat.setWaitingForExpInput(player, true);
                player.sendMessage(
                        Component.text("経験値を増減する数値をチャットに入力してください。").color(NamedTextColor.AQUA)
                                .append(Component.text(" 例: \"10\" で +10レベル, \"-5\" で -5レベル").color(NamedTextColor.GRAY))
                );
            }
            case COMPASS -> {
                GameMode current = player.getGameMode();
                if (current == GameMode.SURVIVAL) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(Component.text("ゲームモードをクリエイティブに変更しました。").color(NamedTextColor.GREEN));
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(Component.text("ゲームモードをサバイバルに変更しました。").color(NamedTextColor.GREEN));
                }
                player.closeInventory();
            }
            case SMITHING_TABLE -> {
                Location found = findNearestSmithingTable(player.getLocation(), 100);
                if (found != null) {
                    player.sendMessage(Component.text("最寄りの鍛冶台: ")
                            .append(Component.text("X: " + found.getBlockX() + ", Y: " + found.getBlockY() + ", Z: " + found.getBlockZ()).color(NamedTextColor.AQUA)));
                } else {
                    player.sendMessage(Component.text("近くに鍛冶台は見つかりませんでした。").color(NamedTextColor.RED));
                }
            }
            default -> {
                player.sendMessage(Component.text("このアイテムにはアクションが設定されていません。").color(NamedTextColor.RED));
            }
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

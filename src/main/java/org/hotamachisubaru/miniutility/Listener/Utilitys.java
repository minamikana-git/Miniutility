package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.Miniutility;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utilitys implements Listener {

    private final boolean creeperProtectionEnabled = false;
    private static final Set<UUID> recentlyTeleported = new HashSet<>();

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title()).trim();

        if (event.getClickedInventory() == null) return;

        // Miniutilityが管理するGUIだけを処理（それ以外は絶対に何もしない！）
        switch (title) {
            case "メニュー" -> {
                event.setCancelled(true);
                handleUtilityBox(player, event.getCurrentItem(), event);
            }
            case "ゴミ箱" -> {
                handleTrashBox(player, event.getCurrentItem(), event);
            }
            case "本当に捨てますか？" -> {
                TrashConfirm(player, event.getCurrentItem(), event);
            }
            case "ニックネームを変更" -> {
                handleNicknameMenu(player, event.getCurrentItem(), event);
            }
            default -> {
                // それ以外（通常チェスト等）は何もしない
            }
        }
    }

    // メニューGUIのアイテムクリック処理
    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");

        switch (clickedItem.getType()) { //選択
            case ARMOR_STAND -> {
                // 死亡地点へワープ
                teleportToDeathLocation(player);
            }
            case ENDER_CHEST -> {
                // エンダーチェストを開く
                player.openInventory(player.getEnderChest());
            }
            case CRAFTING_TABLE -> {
                // 作業台を開く
                player.openWorkbench(null, true);
            }
            case DROPPER -> {
                // ゴミ箱を開く
                openTrashBox(player);
            }
            case NAME_TAG -> {
                // ニックネーム変更GUIを開く
                GUI.NicknameMenu(player);
            }
            case CREEPER_HEAD -> {
                CreeperProtectionListener creeperProtection = plugin.getCreeperProtectionListener();

                // toggleで新しい状態を受け取る
                boolean enabled = creeperProtection.toggleCreeperProtection();
                String status = enabled ? "有効" : "無効";
                player.sendMessage(Component.text("クリーパーの爆破によるブロック破壊防止が " + status + " になりました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }

            case EXPERIENCE_BOTTLE -> {
                player.closeInventory();
                Chat.setWaitingForExpInput(player, true);
                player.sendMessage(
                        Component.text("経験値を増減する数値をチャットに入力してください。").color(NamedTextColor.AQUA)
                                .append(Component.text(" 例: \"10\" で +10レベル, \"-5\" で -5レベル").color(NamedTextColor.GRAY))
                );
                break;
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
                break;
            }


            case SMITHING_TABLE -> {
                // 鍛冶台座標検索
                Location found = findNearestSmithingTable(player.getLocation(), 100);
                if (found != null) {
                    player.sendMessage(Component.text("最寄りの鍛冶台: ")
                            .append(Component.text("X: " + found.getBlockX() + ", Y: " + found.getBlockY() + ", Z: " + found.getBlockZ()).color(NamedTextColor.AQUA)));
                } else {
                    player.sendMessage(Component.text("近くに鍛冶台は見つかりませんでした。").color(NamedTextColor.RED));
                }
                break;
            }

        }
    }

    // ゴミ箱GUIのアイテムクリック処理
    private void handleTrashBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        if (!PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equals("ゴミ箱")) return;

        int clickedSlot = event.getRawSlot();
        if (clickedItem == null) return;
        if (clickedSlot == 53 && clickedItem.getType() == Material.LIME_CONCRETE) {
            event.setCancelled(true);
            confirm(player);
        } else {
            event.setCancelled(false); // それ以外は移動許可
        }
    }

    // ゴミ箱→確認画面
    private void confirm(Player player) {
        Inventory confirmInventory = Bukkit.createInventory(player, 27, Component.text("本当に捨てますか？"));
        confirmInventory.setItem(11, createMenuItem(Material.LIME_CONCRETE, "はい"));
        confirmInventory.setItem(15, createMenuItem(Material.RED_CONCRETE, "いいえ"));
        player.openInventory(confirmInventory);
    }

    // ゴミ箱確認画面
    private void TrashConfirm(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickedItem == null) return;

        switch (clickedItem.getType()) {
            case LIME_CONCRETE -> {
                player.closeInventory();
                // 削除メッセージなどはここで処理
            }
            case RED_CONCRETE -> {
                player.closeInventory();
                player.sendMessage(Component.text("削除をキャンセルしました").color(NamedTextColor.YELLOW));
            }
            default -> {
                player.sendMessage(Component.text("無効な選択です。").color(NamedTextColor.RED));
            }
        }
    }

    // ニックネーム変更GUI
    private void handleNicknameMenu(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        switch (clickedItem.getType()) {
            case PAPER -> {
                player.sendMessage(Component.text("新しいニックネームをチャットに入力してください。").color(NamedTextColor.AQUA));
                Chat.setWaitingForNickname(player, true); // Chatクラスの実装にあわせて有効化
                player.closeInventory();
            }
            case BARRIER -> {
                NicknameDatabase.saveNickname(player.getUniqueId().toString(), "");
                NicknameManager.applyFormattedDisplayName(player);
                player.sendMessage(Component.text("ニックネームをリセットしました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }
            default -> player.sendMessage(Component.text("無効な選択です。").color(NamedTextColor.RED));
        }
    }

    // 死亡地点ワープ
    public static void teleportToDeathLocation(Player player) {
        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");
        if (plugin == null) {
            player.sendMessage(Component.text("プラグインが読み込まれていません。").color(NamedTextColor.RED));
            return;
        }

        var deathLocation = plugin.getDeathLocation(player.getUniqueId());
        if (deathLocation == null) {
            player.sendMessage(Component.text("死亡地点が見つかりません。").color(NamedTextColor.RED));
            return;
        }

        player.teleport(deathLocation);
        if (!recentlyTeleported.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("死亡地点にワープしました。").color(NamedTextColor.GREEN));
            recentlyTeleported.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyTeleported.remove(player.getUniqueId()), 20L);
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

    // ゴミ箱を開く
    private void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        ItemStack confirmButton = createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "捨てる");
        trashInventory.setItem(53, confirmButton);
        player.openInventory(trashInventory);
    }

    // メニュー用アイテム作成
    private ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(NamedTextColor.YELLOW));
            item.setItemMeta(meta);
        }
        return item;
    }
}

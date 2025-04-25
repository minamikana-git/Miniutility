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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;
import org.hotamachisubaru.miniutility.Miniutility;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utility implements Listener {

    private final boolean creeperProtectionEnabled = false;
    private static final Set<UUID> recentlyTeleported = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        // プレイヤー以外は無視
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 表示タイトルを取得
        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title()).trim();

        // ★ここで自作 GUI タイトル以外は何もしない★
        // 「メニュー」「ゴミ箱」「本当に捨てますか？」「ニックネームを変更」
        if (!title.equals("メニュー")
                && !title.equals("ゴミ箱")
                && !title.equals("本当に捨てますか？")
                && !title.equals("ニックネームを変更")) {
            // チェストやエンダーチェストなどは何もせず、通常の動作のまま
            return;
        }

        // 以下、自作 GUI 内のクリック処理
        // ——————
        // ゴミ箱はアイテム移動を許可するだけ
        if (title.equals("ゴミ箱")) {
            // event.setCancelled(false) はデフォルトなので不要
            return;
        }

        // その他の自作 GUI はクリックごと完全キャンセルしてからハンドラへ流す
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        switch (title) {
            case "メニュー" -> handleUtilityBox(player, clicked, event);
            case "本当に捨てますか？" -> TrashConfirm(player, clicked, event);
            case "ニックネームを変更" -> handleNicknameMenu(player, clicked, event);
        }
    }


    private void handleNicknameMenu(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        switch (clickedItem.getType()) {
            case PAPER -> {
                player.sendMessage(Component.text("新しいニックネームをチャットに入力してください。").color(NamedTextColor.AQUA));
                Chat.setWaitingForNickname(player, true);
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

    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");

        switch (clickedItem.getType()) {
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
                UtilityGUI.openNicknameMenu(player);
            }
            case CREEPER_HEAD -> {

                CreeperProtectionListener creeperProtection = plugin.getCreeperProtectionListener();

                // クリーパーの爆破防止をトグルする
                creeperProtection.toggleCreeperProtection();
                String status = creeperProtection.isCreeperProtectionEnabled() ? "有効" : "無効";
                player.sendMessage(Component.text("クリーパーの爆破によるブロック破壊防止が " + status + " になりました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }



        }
    }

    public static void teleportToDeathLocation(Player player) {
        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");
        if (plugin == null) {
            player.sendMessage(Component.text("プラグインが読み込まれていません。").color(NamedTextColor.RED));
            return;
        }

       Location deathLoc = plugin.getDeathLocation(player.getUniqueId());
        if (deathLoc == null) {
            player.sendMessage(Component.text("死亡地点が見つかりません。").color(NamedTextColor.RED));
            return;
        }

        World world = deathLoc.getWorld();
        int x = deathLoc.getBlockX();
        int z = deathLoc.getBlockZ();

        int safeY = world.getHighestBlockYAt(x,z) + 1;
        Location safeLoc = new Location(world, x + 0.5,safeY,z + 0.5);

        player.teleport(safeLoc);
        if (!recentlyTeleported.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("近くの安全な場所にテレポートしました。").color(NamedTextColor.GREEN));
            recentlyTeleported.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin,() -> recentlyTeleported.remove(player.getUniqueId()),20L);

        }
    }

    private void openTrashBox(Player player) {
        Inventory trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        ItemStack confirmButton = createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "捨てる");
        trashInventory.setItem(53, confirmButton);
        player.openInventory(trashInventory);
    }

    private void handleTrashBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        // ゴミ箱のタイトルが一致しない場合は処理しない
        if (!PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equals("ゴミ箱")) return;

        int clickedSlot = event.getRawSlot();
        if (clickedSlot == 53 && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            confirm(player);
        } else {
            event.setCancelled(false); // 通常のスロットはアイテム移動を許可
        }
    }


    private void confirm(Player player) {
        Inventory confirmInventory = Bukkit.createInventory(player, 27, Component.text("本当に捨てますか？"));
        confirmInventory.setItem(11, createMenuItem(Material.GREEN_STAINED_GLASS_PANE, "はい"));
        confirmInventory.setItem(15, createMenuItem(Material.RED_STAINED_GLASS_PANE, "いいえ"));
        player.openInventory(confirmInventory);
    }

    private void TrashConfirm(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickedItem == null) return;

        switch (clickedItem.getType()) {
            case LIME_CONCRETE -> {
                player.closeInventory();
                player.sendMessage(Component.text("削除しました").color(NamedTextColor.BLUE));
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
        

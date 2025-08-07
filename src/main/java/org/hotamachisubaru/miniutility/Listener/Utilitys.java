package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utilitys {

    // 再ワープ防止
    private static final Set<UUID> recentlyTeleported = new HashSet<>();
    private final MiniutilityLoader plugin;
    private final NicknameManager nicknameManager;

    public Utilitys(MiniutilityLoader plugin, NicknameManager nicknameManager) {
        this.plugin = plugin;
        this.nicknameManager = nicknameManager;
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        String title = getTitleSafe(event.getView().title(), event.getView().getTitle());

        // Miniutilityが管理するGUIだけを処理
        switch (title) {
            case "メニュー" -> {
                event.setCancelled(true);
                handleUtilityBox(player, event.getCurrentItem(), event);
            }
            case "ゴミ箱" -> handleTrashBox(player, event.getCurrentItem(), event);
            case "本当に捨てますか？" -> TrashConfirm(player, event.getCurrentItem(), event);
            case "ニックネームを変更" -> handleNicknameMenu(player, event.getCurrentItem(), event);
            default -> { /* 何もしない */ }
        }
    }

    // Paper/Spigot全バージョン両対応のタイトル取得
    private String getTitleSafe(Component componentTitle, String stringTitle) {
        try {
            return PlainTextComponentSerializer.plainText().serialize(componentTitle).trim();
        } catch (Throwable e) {
            return stringTitle.trim();
        }
    }

    // メニューGUIのアイテムクリック処理
    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        event.setCancelled(true);

        switch (clickedItem.getType()) {
            case ARMOR_STAND -> teleportToDeathLocation(player);
            case ENDER_CHEST -> player.openInventory(player.getEnderChest());
            case CRAFTING_TABLE -> player.openWorkbench(null, true);
            case DROPPER -> openTrashBox(player);
            case NAME_TAG -> GUI.NicknameMenu(player);
            case CREEPER_HEAD -> {
                var creeperProtection = plugin.getMiniutility().getCreeperProtectionListener();
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
            default -> player.sendMessage(Component.text("このアイテムにはアクションが設定されていません。").color(NamedTextColor.RED));
        }
    }

    // ゴミ箱GUIのアイテムクリック処理
    private void handleTrashBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        String title = getTitleSafe(event.getView().title(), event.getView().getTitle());
        if (!title.equals("ゴミ箱")) return;
        int clickedSlot = event.getRawSlot();
        if (clickedItem == null) return;
        if (clickedSlot == 53 && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            confirm(player);
        } else {
            event.setCancelled(false);
        }
    }

    // ゴミ箱→確認画面
    private void confirm(Player player) {
        Inventory confirmInventory;
        try {
            confirmInventory = Bukkit.createInventory(player, 27, Component.text("本当に捨てますか？"));
        } catch (Throwable e) {
            confirmInventory = Bukkit.createInventory(player, 27, "本当に捨てますか？");
        }
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
                player.sendMessage(Component.text("アイテムを削除しました。").color(NamedTextColor.GREEN));
                // アイテム削除処理をここに追加
            }
            case RED_CONCRETE -> {
                player.closeInventory();
                player.sendMessage(Component.text("削除をキャンセルしました").color(NamedTextColor.YELLOW));
            }
            default -> player.sendMessage(Component.text("無効な選択です。").color(NamedTextColor.RED));
        }
    }

    // ニックネーム変更GUI
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
                NicknameDatabase.deleteNickname(player);
                NicknameManager.updateDisplayName(player);
                player.sendMessage(Component.text("ニックネームをリセットしました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }
            default -> player.sendMessage(Component.text("無効な選択です。").color(NamedTextColor.RED));
        }
    }

    // 死亡地点ワープ（Paper/Folia両対応: teleportAsync/teleport）
    private void teleportToDeathLocation(Player player) {
        if (plugin.getMiniutility() == null) {
            player.sendMessage(Component.text("プラグイン初期化中です。").color(NamedTextColor.RED));
            return;
        }
        Location deathLocation = plugin.getMiniutility().getDeathLocation(player.getUniqueId());
        if (deathLocation == null) {
            player.sendMessage(Component.text("死亡地点が見つかりません。").color(NamedTextColor.RED));
            return;
        }
        if (APIVersionUtil.isModern()) {
            try {
                Player.class.getMethod("teleportAsync", Location.class).invoke(player, deathLocation);
            } catch (Throwable e) {
                player.teleport(deathLocation);
            }
        } else {
            player.teleport(deathLocation);
        }
        if (recentlyTeleported.add(player.getUniqueId())) {
            player.sendMessage(Component.text("死亡地点にワープしました。").color(NamedTextColor.GREEN));
            Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyTeleported.remove(player.getUniqueId()), 20L);
        }
    }

    // ゴミ箱を開く
    private void openTrashBox(Player player) {
        Inventory trashInventory;
        try {
            trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        } catch (Throwable e) {
            trashInventory = Bukkit.createInventory(player, 54, "ゴミ箱");
        }
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

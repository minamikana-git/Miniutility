package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utility implements Listener {

    private final boolean creeperProtectionEnabled = false;
    private static final Set<UUID> recentlyTeleported = new HashSet<>();

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title()).trim();

        if (event.getClickedInventory() == null) return;

        // **ゴミ箱ではアイテム移動を許可**
        if (title.equalsIgnoreCase("ゴミ箱")) {
            event.setCancelled(false);
            return;
        }

        // **プレイヤーのインベントリでアイテム移動を許可**
        if (event.getClickedInventory().equals(player.getInventory())) {
            event.setCancelled(false);
            return;
        }

        // **それ以外のケースではキャンセル**
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        switch (title.toLowerCase()) {
            // case "メニュー" -> {   ← ここを削除！
            //     handleUtilityBox(player, clickedItem, event);
            // }

            case "ゴミ箱" -> {
                handleTrashBox(player, clickedItem, event);
            }

            case "本当に捨てますか？" -> {
                TrashConfirm(player, clickedItem, event);
            }

            case "ニックネームを変更" -> {
                handleNicknameMenu(player, clickedItem, event);
            }
        }
    }

    // ---------- 以降は従来通り ----------

    private void handleNicknameMenu(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        switch (clickedItem.getType()) {
            case PAPER -> {
                player.sendMessage(Component.text("新しいニックネームをチャットに入力してください。").color(NamedTextColor.AQUA));
                // Chat.setWaitingForNickname(player, true);  // Chatクラス側の実装に合わせて呼び出し
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

    private void handleTrashBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        if (!PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equals("ゴミ箱")) return;

        int clickedSlot = event.getRawSlot();
        if (clickedSlot == 53 && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            confirm(player);
        } else {
            event.setCancelled(false);
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
                // ここで削除メッセージを送らない
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
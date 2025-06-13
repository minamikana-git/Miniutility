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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.hotamachisubaru.miniutility.GUI.UtilityGUI;
import org.hotamachisubaru.miniutility.Miniutility;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrashClickListener implements Listener {

    private static final Component TRASH_BOX_TITLE = Component.text("ゴミ箱");
    private static final Component TRASH_CONFIRM_TITLE = Component.text("本当に捨てますか？");


    private final Set<UUID> deletedPlayers = new HashSet<>();
    private final Plugin plugin;

    public TrashClickListener(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * ゴミ箱インベントリ内のクリックイベントを処理します。
     * 「ゴミ箱」インベントリでは、通常のアイテム移動を許可し、削除ボタンのみを制限します。
     */


    @EventHandler
    public void onTrashBoxClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.equals("ゴミ箱")) {
            // ゴミ箱インベントリ内でのみキャンセル
            if (event.getSlot() == 53 && event.getCurrentItem().getType() == Material.LIME_CONCRETE) {
                event.setCancelled(true);
                UtilityGUI.openTrashConfirm((Player) event.getWhoClicked());
            } else {
                event.setCancelled(false);  // 他のアイテムは移動可能
            }
        } else {
            event.setCancelled(false);  // 他のインベントリではキャンセルしない
        }
    }




    /**
     * ゴミ箱確認インベントリ内のクリックイベントを処理します。
     */


    @EventHandler
    public void onTrashConfirm(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equalsIgnoreCase("本当に捨てますか？")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        // **すでに削除済みの場合は処理しない**
        if (player.hasMetadata("trash_deleted")) {
            return;
        }

        switch (clickedItem.getType()) {
            case LIME_CONCRETE -> {
                // **削除フラグを設定**
                player.setMetadata("trash_deleted", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

                // **削除メッセージを1回だけ送信**
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(Component.text("アイテムを削除しました。", NamedTextColor.RED));
                });

                // **1 tick 後にインベントリを閉じる**
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.closeInventory(), 1L);
            }

            case RED_CONCRETE -> {
                player.sendMessage(Component.text("削除をキャンセルしました。", NamedTextColor.YELLOW));
                player.closeInventory();
            }
            default -> {
                player.sendMessage(Component.text("無効な選択です。", NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Miniutility plugin = (Miniutility) Bukkit.getPluginManager().getPlugin("Miniutility");

        if (plugin == null) return;

        // **削除フラグのリセット**
        if (player.hasMetadata("trash_deleted")) {
            player.removeMetadata("trash_deleted", plugin);
        }
    }

}
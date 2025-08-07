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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.util.FoliaUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hotamachisubaru.miniutility.GUI.GUI.createMenuItem;

public class TrashListener implements Listener {
    private static final Map<UUID, Inventory> lastTrashBox = new HashMap<>();
    private static final Map<UUID, ItemStack[]> trashBoxCache = new HashMap<>();
    private final MiniutilityLoader plugin;

    public TrashListener(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    // タイトル取得（InventoryViewのみ）
    private static String getTitleSafe(@NotNull InventoryView view) {
        try {
            // Paper新API
            return PlainTextComponentSerializer.plainText().serialize(view.title()).trim();
        } catch (Throwable e) {
            // 旧API
            try {
                return view.getTitle().trim();
            } catch (Throwable ignore) {
                return "";
            }
        }
    }

    // ゴミ箱GUIを開く
    public static void openTrashBox(Player player) {
        Inventory trashInventory;
        try {
            trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
        } catch (Throwable e) {
            trashInventory = Bukkit.createInventory(player, 54, "ゴミ箱");
        }
        ItemStack confirmButton = new ItemStack(Material.LIME_CONCRETE);
        var meta = confirmButton.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("捨てる").color(NamedTextColor.RED));
            confirmButton.setItemMeta(meta);
        }
        trashInventory.setItem(53, confirmButton);
        lastTrashBox.put(player.getUniqueId(), trashInventory);
        player.openInventory(trashInventory);
    }

    // 確認画面を開く
    private static void openTrashConfirm(Player player) {
        // ゴミ箱内容を一時保存
        Inventory last = lastTrashBox.get(player.getUniqueId());
        if (last != null) {
            trashBoxCache.put(player.getUniqueId(), last.getContents());
        }

        Inventory confirmMenu;
        try {
            confirmMenu = Bukkit.createInventory(player, 9, Component.text("本当に捨てますか？"));
        } catch (Throwable e) {
            confirmMenu = Bukkit.createInventory(player, 9, "本当に捨てますか？");
        }
        confirmMenu.setItem(3, createMenuItem(Material.LIME_CONCRETE, "はい", "クリックしてゴミ箱を空にする"));
        confirmMenu.setItem(5, createMenuItem(Material.RED_CONCRETE, "いいえ", "クリックしてキャンセル"));
        player.openInventory(confirmMenu);
    }

    @EventHandler
    public void onTrashBoxClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = getTitleSafe(event.getView());

        if (title.equals("ゴミ箱")) {
            int rawSlot = event.getRawSlot();
            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            // 捨てるボタンは絶対キャンセル
            if (rawSlot == 53 && item.getType() == Material.LIME_CONCRETE) {
                event.setCancelled(true);
                openTrashConfirm(player);
                return;
            }

            // ゴミ箱上段スロット (0-52) かつ「捨てるボタン以外」
            if (rawSlot >= 0 && rawSlot < 53) {
                event.setCancelled(false);
                return;
            }

            // プレイヤーのインベントリ側（下段）
            if (rawSlot >= 54) {
                event.setCancelled(false);
                return;
            }

            // その他はキャンセル
            event.setCancelled(true);
            return;
        }

        // --- 確認画面 ---
        if (title.equals("本当に捨てますか？")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            if (item.getType() == Material.LIME_CONCRETE) {
                // 削除
                Inventory prev = lastTrashBox.get(player.getUniqueId());
                if (prev != null) {
                    FoliaUtil.runAtPlayer(plugin, player, () -> {
                        for (int i = 0; i < 53; i++) prev.setItem(i, null);
                        player.closeInventory();
                        player.sendMessage(Component.text("ゴミ箱のアイテムをすべて削除しました。").color(NamedTextColor.GREEN));
                    });
                    lastTrashBox.remove(player.getUniqueId());
                    trashBoxCache.remove(player.getUniqueId());
                }
            } else if (item.getType() == Material.RED_CONCRETE) {
                // 復元処理
                ItemStack[] cache = trashBoxCache.get(player.getUniqueId());
                if (cache != null) {
                    Inventory trashInventory;
                    try {
                        trashInventory = Bukkit.createInventory(player, 54, Component.text("ゴミ箱"));
                    } catch (Throwable e) {
                        trashInventory = Bukkit.createInventory(player, 54, "ゴミ箱");
                    }
                    // アイテムを復元（0-52のみ！）
                    for (int i = 0; i < 53; i++) {
                        trashInventory.setItem(i, (i < cache.length) ? cache[i] : null);
                    }
                    // 53番は必ず「捨てる」ボタン
                    ItemStack confirmButton = new ItemStack(Material.LIME_CONCRETE);
                    var meta = confirmButton.getItemMeta();
                    if (meta != null) {
                        meta.displayName(Component.text("捨てる").color(NamedTextColor.RED));
                        confirmButton.setItemMeta(meta);
                    }
                    trashInventory.setItem(53, confirmButton);

                    lastTrashBox.put(player.getUniqueId(), trashInventory);
                    player.openInventory(trashInventory);
                } else {
                    player.closeInventory();
                }
                trashBoxCache.remove(player.getUniqueId());
                player.sendMessage(Component.text("削除をキャンセルしました。").color(NamedTextColor.YELLOW));
            }
        }
    }
}

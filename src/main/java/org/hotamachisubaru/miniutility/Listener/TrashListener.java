package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.holder.GuiHolder;
import org.hotamachisubaru.miniutility.GUI.holder.GuiType;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

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

    // ゴミ箱GUIを開く
    public static void openTrashBox(Player player) {
        GuiHolder h = new GuiHolder(GuiType.TRASH, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(h, 54, "ゴミ箱");
        h.bind(inv);

        // 53番に「捨てる」ボタン
        ItemStack confirm = createMenuItem(Material.LIME_WOOL,
                ChatColor.RED + "捨てる",
                ChatColor.GRAY + "クリックして削除確認へ");
        inv.setItem(53, confirm);

        lastTrashBox.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    // 確認画面を開く
    private static void openTrashConfirm(Player player) {
        Inventory last = lastTrashBox.get(player.getUniqueId());
        if (last != null) {
            trashBoxCache.put(player.getUniqueId(), last.getContents());
        }

        GuiHolder h = new GuiHolder(GuiType.TRASH_CONFIRM, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(h, 9, "本当に捨てますか？");
        h.bind(inv);

        inv.setItem(3, createMenuItem(Material.LIME_WOOL, ChatColor.GREEN + "はい", ChatColor.GRAY + "ゴミ箱を空にする"));
        inv.setItem(5, createMenuItem(Material.RED_WOOL, ChatColor.RED + "いいえ", ChatColor.GRAY + "キャンセル"));
        player.openInventory(inv);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrashClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof GuiHolder h)) return;

        ItemStack item = event.getCurrentItem();
        int rawSlot = event.getRawSlot();

        // --- ゴミ箱本体 ---
        if (h.getType() == GuiType.TRASH) {
            // 53番の「捨てる」ボタンは固定
            if (rawSlot == 53) {
                event.setCancelled(true);
                if (item != null && item.getType() == Material.LIME_WOOL) {
                    openTrashConfirm(player);
                }
                return;
            }

            // 上段(0-52)は自由に出し入れOK
            if (rawSlot >= 0 && rawSlot < 53) {
                event.setCancelled(false);
                return;
            }

            // 下段(プレイヤーインベントリ)もそのまま
            event.setCancelled(false);
            return;
        }

        // --- 確認画面 ---
        if (h.getType() == GuiType.TRASH_CONFIRM) {
            event.setCancelled(true);
            if (item == null || item.getType().isAir()) return;

            if (item.getType() == Material.LIME_WOOL) {
                // 中身を完全削除
                Inventory prev = lastTrashBox.get(player.getUniqueId());
                if (prev != null) {
                    FoliaUtil.runAtPlayer(plugin, player.getUniqueId(), () -> {
                        for (int i = 0; i < 53; i++) prev.setItem(i, null);
                        player.closeInventory();
                        player.sendMessage(ChatColor.GREEN + "ゴミ箱のアイテムをすべて削除しました。");
                    });
                    lastTrashBox.remove(player.getUniqueId());
                    trashBoxCache.remove(player.getUniqueId());
                } else {
                    player.closeInventory();
                }
                return;
            }

            if (item.getType() == Material.RED_WOOL) {
                // 復元
                ItemStack[] cache = trashBoxCache.get(player.getUniqueId());
                GuiHolder nh = new GuiHolder(GuiType.TRASH, player.getUniqueId());
                Inventory newInv = Bukkit.createInventory(nh, 54, "ゴミ箱");
                nh.bind(newInv);

                if (cache != null) {
                    for (int i = 0; i < 53; i++) {
                        newInv.setItem(i, (i < cache.length) ? cache[i] : null);
                    }
                }
                newInv.setItem(53, createMenuItem(Material.LIME_WOOL, ChatColor.RED + "捨てる", ChatColor.GRAY + "クリックして削除確認へ"));

                lastTrashBox.put(player.getUniqueId(), newInv);
                player.openInventory(newInv);
                trashBoxCache.remove(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "削除をキャンセルしました。");
            }
        }
    }
}

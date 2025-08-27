package org.hotamachisubaru.miniutility.Listener;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
                "§c捨てる",          // RED
                "§7クリックして削除確認へ"  // GRAY
        );
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

        inv.setItem(3, createMenuItem(
                Material.LIME_WOOL,
                "§aはい",          // 緑色
                "§7ゴミ箱を空にする" // グレー
        ));
        inv.setItem(5, createMenuItem(
                Material.RED_WOOL,
                "§cいいえ",        // 赤色
                "§7キャンセル"      // グレー
        ));

        player.openInventory(inv);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrashClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof GuiHolder)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        // --- ゴミ箱本体 ---
        if (((GuiHolder) holder).getType() == GuiType.TRASH) {
            int raw = event.getRawSlot();

            // 53 は「捨てる」ボタン
            if (raw == 53) {
                event.setCancelled(true);
                if (item.getType() == Material.LIME_WOOL) {
                    openTrashConfirm(player);
                }
                return;
            }

            // 上段(0-52)と下段(>=54)は自由
            event.setCancelled(false);
            return;
        }

        // --- 確認画面 ---
        if (((GuiHolder) holder).getType() == GuiType.TRASH_CONFIRM) {
            event.setCancelled(true);

            if (item.getType() == Material.LIME_WOOL) {
                Inventory prev = lastTrashBox.get(player.getUniqueId());
                if (prev != null) {
                    FoliaUtil.runAtPlayer(plugin, player.getUniqueId(), () -> {
                        for (int i = 0; i < 53; i++) prev.setItem(i, null);
                        player.closeInventory();

                        TextComponent component = new TextComponent();
                        component.setText("ゴミ箱のアイテムをすべて削除しました。");
                        component.setColor(ChatColor.GREEN.asBungee());

                        player.sendMessage(component);
                    });
                    lastTrashBox.remove(player.getUniqueId());
                    trashBoxCache.remove(player.getUniqueId());
                } else {
                    player.closeInventory();
                }
                return;
            }

            if (item.getType() == Material.RED_WOOL) {
                ItemStack[] cache = trashBoxCache.get(player.getUniqueId());
                GuiHolder nh = new GuiHolder(GuiType.TRASH, player.getUniqueId());
                Inventory newInv = Bukkit.createInventory(nh, 54, "ゴミ箱");
                nh.bind(newInv);

                if (cache != null) {
                    for (int i = 0; i < 53; i++) newInv.setItem(i, (i < cache.length) ? cache[i] : null);
                }
                newInv.setItem(53, createMenuItem(
                        Material.LIME_WOOL,
                        "§c捨てる",          // 赤色
                        "§7クリックして削除確認へ" // グレー
                ));

                lastTrashBox.put(player.getUniqueId(), newInv);
                player.openInventory(newInv);
                trashBoxCache.remove(player.getUniqueId());

                TextComponent component = new TextComponent();
                component.setText("削除をキャンセルしました。");
                component.setColor(ChatColor.YELLOW.asBungee());

                player.sendMessage(component);
            }
        }
    }

}
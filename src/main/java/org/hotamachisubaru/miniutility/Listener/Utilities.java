package org.hotamachisubaru.miniutility.Listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.GUI.holder.GuiHolder;
import org.hotamachisubaru.miniutility.GUI.holder.GuiType;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utilities implements Listener {

    // 再ワープ防止
    private static final Set<UUID> recentlyTeleported = new HashSet<>();
    private final MiniutilityLoader plugin;
    private final NicknameManager nicknameManager;

    public Utilities(MiniutilityLoader plugin, NicknameManager nicknameManager) {
        this.plugin = plugin;
        this.nicknameManager = nicknameManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) return;
        Inventory top = event.getView().getTopInventory();
        if (event.getClickedInventory() != top) return;

        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof GuiHolder)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return; // 1.13互換

        // 文字列タイトルではなく Holder の種類で分岐
        switch (((GuiHolder) holder).getType()) {
            case MENU:
                event.setCancelled(true);
                handleUtilityBox(player, clicked, event);
                break;

            case TRASH:
                handleTrashBox(player, clicked, event);
                break;

            case TRASH_CONFIRM:
                handleTrashConfirm(player, clicked, event);
                break;

            case NICKNAME:
                handleNicknameMenu(player, clicked, event);
                break;

            default:
                // 何もしない
                break;
        }
    }

    // メニューGUIのアイテムクリック処理
    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        switch (clickedItem.getType()) {
            case ARMOR_STAND:
                event.setCancelled(true);
                teleportToDeathLocation(player);
                break;

            case ENDER_CHEST:
                event.setCancelled(true);
                player.openInventory(player.getEnderChest());
                break;

            case CRAFTING_TABLE:
                event.setCancelled(true);
                player.openWorkbench(null, true);
                break;

            case DROPPER:
                event.setCancelled(true);
                openTrashBox(player);
                break;

            case NAME_TAG:
                event.setCancelled(true);
                GUI.NicknameMenu(player);
                break;

            case CREEPER_HEAD: {
                event.setCancelled(true);
                CreeperProtectionListener cp = plugin.getMiniutility().getCreeperProtectionListener();
                boolean nowEnabled = cp.toggle();

                TextComponent ccomponent = new TextComponent();
                ccomponent.setText("クリーパーの爆破によるブロック破壊防止が " + (nowEnabled ? "有効" : "無効") + " になりました。");
                ccomponent.setColor(ChatColor.GREEN);

                player.sendMessage(ccomponent);
                player.closeInventory();
                break;
            }

            case EXPERIENCE_BOTTLE:
                event.setCancelled(true);
                player.closeInventory();
                Chat.setWaitingForExpInput(player, true);

                TextComponent ecomponent = new TextComponent();
                ecomponent.setText("経験値を増減する数値をチャットに入力してください。");
                ecomponent.setColor(ChatColor.AQUA);

                TextComponent ecomponentChild = new TextComponent();
                ecomponentChild.setText(" 例: \"10\" で +10レベル, \"-5\" で -5レベル");
                ecomponentChild.setColor(ChatColor.GRAY);
                ecomponent.addExtra(ecomponentChild);

                player.sendMessage(ecomponent);
                break;

            case COMPASS:
                event.setCancelled(true);
                GameMode current = player.getGameMode();
                if (current == GameMode.SURVIVAL) {
                    player.setGameMode(GameMode.CREATIVE);

                    TextComponent component = new TextComponent();
                    component.setText("ゲームモードをクリエイティブに変更しました。");
                    component.setColor(ChatColor.GREEN);

                    player.sendMessage(component);
                } else {
                    player.setGameMode(GameMode.SURVIVAL);

                    TextComponent component = new TextComponent();
                    component.setText("ゲームモードをサバイバルに変更しました。");
                    component.setColor(ChatColor.GREEN);

                    player.sendMessage(component);
                }
                player.closeInventory();
                break;

            default:
                event.setCancelled(true);

                TextComponent dcomponent = new TextComponent();
                dcomponent.setText("このアイテムにはアクションが設定されていません。");
                dcomponent.setColor(ChatColor.RED);

                player.sendMessage(dcomponent);
                break;
        }
    }

    // ゴミ箱GUIのアイテムクリック処理（本体）
    private void handleTrashBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // 53番は「捨てる」ボタン
        if (rawSlot == 53 && clickedItem.getType() == Material.LIME_WOOL) {
            event.setCancelled(true);
            openTrashConfirm(player);
            return;
        }

        // 上段(0-52)・下段は自由に移動可
        event.setCancelled(false);
    }

    // ゴミ箱→確認画面を開く
    private static void openTrashConfirm(Player player) {
        GuiHolder h = new GuiHolder(GuiType.TRASH_CONFIRM, player.getUniqueId());
        Inventory confirm = Bukkit.createInventory(h, 9, "本当に捨てますか？");
        h.bind(confirm);

        confirm.setItem(3, createMenuItem(Material.LIME_WOOL, "§aはい")); // 緑色
        confirm.setItem(5, createMenuItem(Material.RED_WOOL, "§cいいえ")); // 赤色
        player.openInventory(confirm);
    }

    // 確認画面のクリック処理
    private void handleTrashConfirm(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        if (clickedItem.getType() == Material.LIME_WOOL) {
            // 実際の削除は上段のみ（0-52）
            Inventory last = player.getOpenInventory().getTopInventory();
            for (int i = 0; i < 53; i++) last.setItem(i, null);
            player.closeInventory();

            TextComponent component = new TextComponent();
            component.setText("ゴミ箱のアイテムをすべて削除しました。");
            component.setColor(ChatColor.GREEN);

            player.sendMessage(component);
            return;
        }

        if (clickedItem.getType() == Material.RED_WOOL) {
            // キャンセル：単に閉じる
            TextComponent component = new TextComponent();
            component.setText("削除をキャンセルしました。");
            component.setColor(ChatColor.YELLOW);

            player.closeInventory();
            player.sendMessage(component);
        }
    }

    // ニックネーム変更GUI
    private void handleNicknameMenu(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        event.setCancelled(true);

        switch (clickedItem.getType()) {
            case PAPER:
                TextComponent pcomponent = new TextComponent();
                pcomponent.setText("新しいニックネームをチャットに入力してください。");
                pcomponent.setColor(ChatColor.AQUA);

                player.sendMessage(pcomponent);
                Chat.setWaitingForNickname(player, true);
                player.closeInventory();
                break;

            case NAME_TAG:
                TextComponent ncomponent = new TextComponent();
                ncomponent.setText("色付きのニックネームをチャットで入力してください。例: &6ほたまち");
                ncomponent.setColor(ChatColor.AQUA);

                player.sendMessage(ncomponent);
                Chat.setWaitingForColorInput(player, true);
                player.closeInventory();
                break;

            case BARRIER:
                NicknameDatabase.deleteNickname(player);
                NicknameManager.updateDisplayName(player);

                TextComponent bcomponent = new TextComponent();
                bcomponent.setText("ニックネームをリセットしました。");
                bcomponent.setColor(ChatColor.GREEN);

                player.sendMessage(bcomponent);
                player.closeInventory();
                break;

            default:
                TextComponent dcomponent = new TextComponent();
                dcomponent.setText("無効な選択です。");
                dcomponent.setColor(ChatColor.RED);

                player.sendMessage(dcomponent);
                break;
        }
    }

    // 死亡地点ワープ（Paper/Folia両対応）
    private void teleportToDeathLocation(Player player) {
        if (plugin.getMiniutility() == null) {
            TextComponent component = new TextComponent();
            component.setText("プラグイン初期化中です。");
            component.setColor(ChatColor.RED);

            return;
        }
        Location deathLocation = plugin.getMiniutility().getDeathLocation(player.getUniqueId());
        if (deathLocation == null) {
            TextComponent component = new TextComponent();
            component.setText("死亡地点が見つかりません。");
            component.setColor(ChatColor.RED);

            player.sendMessage(component);
            return;
        }

        try {
            // Paper 1.20.1+ にあれば teleportAsync を使う
            player.getClass().getMethod("teleportAsync", Location.class).invoke(player, deathLocation);
        } catch (Throwable t) {
            player.teleport(deathLocation);
        }

        if (recentlyTeleported.add(player.getUniqueId())) {
            TextComponent component = new TextComponent();
            component.setText("死亡地点にワープしました。");
            component.setColor(ChatColor.GREEN);

            player.sendMessage(component);
            // Folia互換の遅延実行
            FoliaUtil.runLater(plugin, new Runnable() {
                @Override
                public void run() {
                    recentlyTeleported.remove(player.getUniqueId());
                }
            }, 20L);
        }
    }

    // シンプルなメニュー用アイテム
    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ゴミ箱を開く（本体）
    private void openTrashBox(Player player) {
        GuiHolder h = new GuiHolder(GuiType.TRASH, player.getUniqueId());
        Inventory inv = Bukkit.createInventory(h, 54, "ゴミ箱");
        h.bind(inv);

        inv.setItem(53, createMenuItem(Material.LIME_WOOL, "§c捨てる"));
        player.openInventory(inv);
    }
}
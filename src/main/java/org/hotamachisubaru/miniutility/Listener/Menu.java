package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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


public class Menu implements Listener {

    private final MiniutilityLoader plugin;

    public Menu (MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof GuiHolder h)) return;
        if (h.getType() != GuiType.MENU) return;   // ★ Holderでメニュー判定

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        // ★ そのままメニューのクリック処理へ
        handleUtilityBox(player, clicked, event);
    }


    // メニューGUIのクリック処理
    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Material type = clickedItem.getType();
        switch (type) {
            case ARMOR_STAND -> teleportToDeathLocation(player);
            case ENDER_CHEST   -> player.openInventory(player.getEnderChest());
            case CRAFTING_TABLE-> player.openWorkbench(null, true);
            case DROPPER       -> TrashListener.openTrashBox(player);
            case NAME_TAG      -> NicknameListener.openNicknameMenu(player);
            case CREEPER_HEAD -> {
                var cp = plugin.getMiniutility().getCreeperProtectionListener();
                boolean nowEnabled = cp.toggle();
                player.sendMessage(ChatColor.GREEN + "クリーパーの爆破によるブロック破壊防止が "
                        + (nowEnabled ? "有効" : "無効") + " になりました。");
                player.closeInventory();
            }

            case EXPERIENCE_BOTTLE -> {
                player.closeInventory();
                Chat.setWaitingForExpInput(player, true);
                player.sendMessage(ChatColor.AQUA + "経験値を増減する数値をチャットに入力してください。"
                        + ChatColor.GRAY + " 例: \"10\" で +10レベル, \"-5\" で -5レベル");
            }
            case COMPASS -> {
                GameMode current = player.getGameMode();
                if (current == GameMode.SURVIVAL) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(ChatColor.GREEN + "ゲームモードをクリエイティブに変更しました。");
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(ChatColor.GREEN + "ゲームモードをサバイバルに変更しました。");
                }
                player.closeInventory();
            }
            default -> player.sendMessage(ChatColor.RED + "このアイテムにはアクションが設定されていません。");
        }
    }

    // 死亡地点ワープ（API差分両対応）
    // 互換テレポート本体（クラス外側に置く：メンバ or static）
    private static void teleportCompat(Player p, Location loc) {
        try {
            // Paper 1.20.1+ only
            var m = p.getClass().getMethod("teleportAsync", Location.class);
            m.invoke(p, loc); // CompletableFuture だが待たずにOK
        } catch (Throwable ignore) {
            // 旧APIへフォールバック
            p.teleport(loc);
        }
    }

    // 死亡地点ワープ（API差分両対応）
    private void teleportToDeathLocation(Player player) {
        if (plugin.getMiniutility() == null) {
            player.sendMessage(ChatColor.RED + "プラグイン初期化中です。");
            return;
        }
        Location loc = plugin.getMiniutility().getDeathLocation(player.getUniqueId());
        if (loc == null) {
            player.sendMessage(ChatColor.RED + "死亡地点が見つかりません。");
            return;
        }

        // Folia/Paper 両対応でプレイヤー領域スレッドで実行（非Foliaなら通常スレッド）
        FoliaUtil.runAtPlayer(
                plugin, player.getUniqueId(),
                () -> {
                    teleportCompat(player, loc);
                    player.sendMessage(ChatColor.GREEN + "死亡地点にワープしました。");
                }
        );
    }

}

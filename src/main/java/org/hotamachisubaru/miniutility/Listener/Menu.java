package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;
import org.hotamachisubaru.miniutility.util.TitleUtil;

public class Menu implements Listener {

    private final MiniutilityLoader plugin;

    public Menu(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        // GUIタイトル（Component/String 両対応）
        final String title = TitleUtil.getTitle(event.getView());
        if (title.isEmpty()) return;

        switch (title) {
            case "メニュー" -> {
                event.setCancelled(true);
                handleUtilityBox(player, event.getCurrentItem(), event);
            }
            case "ゴミ箱" -> {
                // ゴミ箱ではアイテム移動を許可
                event.setCancelled(false);
                Utilitys.handleTrashBox(player, event.getCurrentItem(), event);
            }
            case "本当に捨てますか？" -> {
                event.setCancelled(true);
                Utilitys.TrashConfirm(player, event.getCurrentItem(), event);
            }
            case "ニックネームを変更" -> {
                event.setCancelled(true);
                GUI.NicknameMenu(player);
            }
            default -> { /* 独自GUI以外は何もしない */ }
        }
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
                // 例: Menu.handleUtilityBox の CREEPER_HEAD 分岐内
                var creeperProtection = plugin.getMiniutility().getCreeperProtectionListener();
                boolean nowEnabled = creeperProtection.toggle();
                String status = nowEnabled ? "有効" : "無効";
                player.sendMessage(ChatColor.GREEN + "クリーパーの爆破によるブロック破壊防止が " + status + " になりました。");
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

        // Paper 1.20.1+ の teleportAsync があれば使う（反射）
        if (APIVersionUtil.isModern()) {
            try {
                Player.class.getMethod("teleportAsync", Location.class).invoke(player, loc);
            } catch (Throwable ignored) {
                player.teleport(loc);
            }
        } else {
            player.teleport(loc);
        }
        player.sendMessage(ChatColor.GREEN + "死亡地点にワープしました。");
    }
}

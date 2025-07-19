package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

public class Menu implements Listener {

    private final MiniutilityLoader plugin;

    public Menu(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title()).trim();

        switch (title) {
            case "メニュー" -> {
                event.setCancelled(true);
                handleUtilityBox(player, event.getCurrentItem(), event);
            }
            case "ニックネームを変更" -> {
                event.setCancelled(true);
                // 必要ならここに処理を追加
            }
            default -> { /* 他は何もしない */ }
        }
    }

    // --- メニューGUIのクリックアクション処理 ---
    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        switch (clickedItem.getType()) {
            case ARMOR_STAND -> teleportToDeathLocation(player);
            case ENDER_CHEST -> player.openInventory(player.getEnderChest());
            case CRAFTING_TABLE -> player.openWorkbench(null, true);
            case DROPPER -> TrashListener.openTrashBox(player);
            case NAME_TAG -> NicknameListener.openNicknameMenu(player);
            case CREEPER_HEAD -> {
                var creeperProtection = plugin.getMiniutility().getCreeperProtectionListener();
                boolean enabled = creeperProtection.toggleCreeperProtection();
                String status = enabled ? "有効" : "無効";
                player.sendMessage(Component.text("クリーパーの爆破によるブロック破壊防止が " + status + " になりました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }
            case EXPERIENCE_BOTTLE -> {
                player.closeInventory();
                org.hotamachisubaru.miniutility.Listener.Chat.setWaitingForExpInput(player, true);
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

            case FEATHER -> {
                // 2段ジャンプ状態をトグル
                DoubleJumpListener.toggleDoubleJump(player);
                boolean enabled = DoubleJumpListener.isDoubleJumpEnabled(player.getUniqueId());

                // プレイヤーにフィードバック
                player.sendMessage(
                        Component.text("2段ジャンプが " + (enabled ? "有効" : "無効") + " になりました。")
                                .color(enabled ? NamedTextColor.AQUA : NamedTextColor.RED)
                );

                // メニュー再表示でON/OFFボタンの状態を即反映
                GUI.openMenu(player, plugin);
                player.closeInventory();
            }



            default -> player.sendMessage(Component.text("このアイテムにはアクションが設定されていません。").color(NamedTextColor.RED));
        }
    }

    // 死亡地点ワープ（Folia/Paper両対応）
    private void teleportToDeathLocation(Player player) {
        if (plugin.getMiniutility() == null) {
            player.sendMessage(Component.text("プラグイン初期化中です。").color(NamedTextColor.RED));
            return;
        }
        Location loc = plugin.getMiniutility().getDeathLocation(player.getUniqueId());
        if (loc == null) {
            player.sendMessage(Component.text("死亡地点が見つかりません。").color(NamedTextColor.RED));
            return;
        }
        player.teleportAsync(loc);
        player.sendMessage(Component.text("死亡地点にワープしました。").color(NamedTextColor.GREEN));
    }
}

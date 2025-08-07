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
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.util.APIVersionUtil;

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

        // GUIタイトルの両対応（Component/String）
        String title;
        try {
            // 1.17.x～1.18.xでは通常String、1.19以降はComponentになるケース多い
            title = PlainTextComponentSerializer.plainText().serialize(event.getView().title()).trim();
        } catch (Throwable e) {
            title = event.getView().getTitle().trim();
        }

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

    // --- メニューGUIのクリックアクション処理（分岐吸収済み） ---
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
            default -> player.sendMessage(Component.text("このアイテムにはアクションが設定されていません。").color(NamedTextColor.RED));
        }
    }

    // 死亡地点ワープ（API差分両対応）
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
        // Paper 1.20.1以降のみteleportAsync対応
        if (APIVersionUtil.isModern()) {
            try {
                Player.class.getMethod("teleportAsync", Location.class).invoke(player, loc);
            } catch (Throwable e) {
                player.teleport(loc);
            }
        } else {
            player.teleport(loc);
        }
        player.sendMessage(Component.text("死亡地点にワープしました。").color(NamedTextColor.GREEN));
    }
}

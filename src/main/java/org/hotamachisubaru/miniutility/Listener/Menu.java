package org.hotamachisubaru.miniutility.Listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.hotamachisubaru.miniutility.GUI.holder.GuiHolder;
import org.hotamachisubaru.miniutility.GUI.holder.GuiType;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.util.FoliaUtil;

import java.lang.reflect.Method;


public class Menu implements Listener {

    private final MiniutilityLoader plugin;

    public Menu(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;

        Inventory clicked = event.getClickedInventory();
        InventoryHolder holder = clicked.getHolder();
        if (!(holder instanceof GuiHolder)) return;
        if (((GuiHolder) holder).getType() != GuiType.MENU) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        handleUtilityBox((Player) event.getWhoClicked(), clickedItem, event);
    }


    // メニューGUIのクリック処理
    private void handleUtilityBox(Player player, ItemStack clickedItem, InventoryClickEvent event) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Material type = clickedItem.getType();
        switch (type) {
            case ARMOR_STAND:
                teleportToDeathLocation(player);
                break;
            case ENDER_CHEST:
                player.openInventory(player.getEnderChest());
                break;
            case CRAFTING_TABLE:
                player.openWorkbench(null, true);
                break;
            case DROPPER:
                TrashListener.openTrashBox(player);
                break;
            case NAME_TAG:
                NicknameListener.openNicknameMenu(player);
                break;
            case CREEPER_HEAD: {
                CreeperProtectionListener cp = plugin.getMiniutility().getCreeperProtectionListener();
                boolean nowEnabled = cp.toggle();

                TextComponent component = new TextComponent();
                component.setText("クリーパーの爆破によるブロック破壊防止が " + (nowEnabled ? "有効" : "無効") + " になりました。");
                component.setColor(ChatColor.GREEN);

                player.sendMessage(component);
                player.closeInventory();
                break;
            }

            case EXPERIENCE_BOTTLE: {
                player.closeInventory();
                Chat.setWaitingForExpInput(player, true);

                TextComponent component = new TextComponent();
                component.setText("経験値を増減する数値をチャットに入力してください。");
                component.setColor(ChatColor.AQUA);

                TextComponent childComponent = new TextComponent();
                childComponent.setText(" 例: \"10\" で +10レベル, \"-5\" で -5レベル");
                childComponent.setColor(ChatColor.GRAY);

                component.addExtra(childComponent);

                player.sendMessage(component);
                break;
            }
            case COMPASS: {
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
            }
            default:
                TextComponent component = new TextComponent();
                component.setText("このアイテムにはアクションが設定されていません。");
                component.setColor(ChatColor.RED);

                player.sendMessage(component);
                break;
        }
    }

    // 死亡地点ワープ（API差分両対応）
    // 互換テレポート本体（クラス外側に置く：メンバ or static）
    private static void teleportCompat(Player p, Location loc) {
        try {
            // Paper 1.20.1+ only
            Method m = p.getClass().getMethod("teleportAsync", Location.class);
            m.invoke(p, loc); // CompletableFuture だが待たずにOK
        } catch (Throwable ignore) {
            // 旧APIへフォールバック
            p.teleport(loc);
        }
    }

    // 死亡地点ワープ（API差分両対応）
    private void teleportToDeathLocation(Player player) {
        if (plugin.getMiniutility() == null) {
            TextComponent component = new TextComponent();
            component.setText("プラグイン初期化中です。");
            component.setColor(ChatColor.RED);
            player.sendMessage(component);
            return;
        }
        Location loc = plugin.getMiniutility().getDeathLocation(player.getUniqueId());
        if (loc == null) {
            TextComponent component = new TextComponent();
            component.setText("死亡地点が見つかりません。");
            component.setColor(ChatColor.RED);
            player.sendMessage(component);
            return;
        }

        // Folia/Paper 両対応でプレイヤー領域スレッドで実行（非Foliaなら通常スレッド）
        FoliaUtil.runAtPlayer(
                plugin, player.getUniqueId(),
                () -> {
                    teleportCompat(player, loc);
                    TextComponent component = new TextComponent();
                    component.setText("死亡地点にワープしました。");
                    component.setColor(ChatColor.GREEN);
                    player.sendMessage(component);
                }
        );
    }

}
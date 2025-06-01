package org.hotamachisubaru.miniutility.Listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.hotamachisubaru.miniutility.Miniutility;

public class DeathListener implements Listener {

    private final Miniutility plugin;

    public DeathListener(Miniutility plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void saveDeathLocation(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // --- 1. 死亡地点を取得して、それを保存する ---
        Location deathLoc = player.getLocation().getBlock().getLocation().add(0, 1, 0);
        plugin.setDeathLocation(player.getUniqueId(), deathLoc);

        // --- 2. ダブルチェストを置く ---
        //    (Simple に X 軸方向に二つ並べてチェストにする例)
        Block blockLeft  = deathLoc.getBlock();
        Block blockRight = deathLoc.clone().add(1, 0, 0).getBlock();

        // 左側のブロックをチェストにする
        blockLeft.setType(Material.CHEST);
        Chest chestLeft = (Chest) blockLeft.getState();

        // 右側のブロックをチェストにする
        blockRight.setType(Material.CHEST);
        Chest chestRight = (Chest) blockRight.getState();

        // これで二つ並べると自動的にダブルチェストになる
        DoubleChest doubleChest = (DoubleChest) chestLeft.getInventory().getHolder();
        // ↑チェストを二つ並べると、左のChest#getInventory().getHolder() が DoubleChest となる

        // --- 3. プレイヤーの全持ち物を取り出してダブルチェストに詰め込む ---
        PlayerInventory inv = player.getInventory();

        // (1) メインインベントリ + ホットバー
        ItemStack[] mainContents = inv.getContents();
        // (2) アーマー（ヘルメット、チェストプレート、レギンス、ブーツ）
        ItemStack[] armorContents = inv.getArmorContents();
        // (3) オフハンド
        ItemStack offHandItem = inv.getItemInOffHand();

        // (4) ダブルチェスト（54スロット）にまとめて addItem する
        //     addItem は溢れると Map<ItemStack,Integer> で余剰分を返すが、
        //     ここではすべて収まる想定なので戻り値は気にしない
        if (doubleChest != null) {
            // まずメインインベントリ
            for (ItemStack stack : mainContents) {
                if (stack != null && stack.getType() != Material.AIR) {
                    doubleChest.getInventory().addItem(stack.clone());
                }
            }
            // 次にアーマー
            for (ItemStack stack : armorContents) {
                if (stack != null && stack.getType() != Material.AIR) {
                    doubleChest.getInventory().addItem(stack.clone());
                }
            }
            // オフハンド
            if (offHandItem != null && offHandItem.getType() != Material.AIR) {
                doubleChest.getInventory().addItem(offHandItem.clone());
            }
        }

        // --- 4. イベントのドロップをクリアして、地面にアイテムが散らばらないようにする ---
        event.getDrops().clear();

        // --- 5. さらに、念のためプレイヤーの中身も空にしておく（リスポーン後にアイテムが残らないように） ---
        inv.clear();
        inv.setArmorContents(new ItemStack[]{null, null, null, null});
        inv.setItemInOffHand(null);
    }
}

package org.hotamachisubaru.miniutility.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;
import org.hotamachisubaru.miniutility.Nickname.NicknameManager;

public class NicknameClickListener implements Listener {

    @EventHandler
    public void onNicknameMenuClick(InventoryClickEvent event) {
        // インベントリタイトルを取得
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals("ニックネーム変更")) return; // ニックネームメニュー以外なら処理しない

        event.setCancelled(true); // インベントリのアイテム移動を防止
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        switch (clickedItem.getType()) {
            case PAPER -> {
                // チャットでニックネームを入力
                player.sendMessage(Component.text("新しいニックネームをチャットで入力してください。").color(NamedTextColor.AQUA));
                NicknameManager.setWaitingForNickname(player, true);
                player.closeInventory(); // GUIを閉じる
            }
            case BARRIER -> {
                // ニックネームをリセット
                NicknameDatabase.saveNickname(player.getUniqueId().toString(), "");
                NicknameManager.applyFormattedDisplayName(player);
                player.sendMessage(Component.text("ニックネームをリセットしました。").color(NamedTextColor.GREEN));
                player.closeInventory();
            }
            default -> {
                player.sendMessage(Component.text("無効な選択です。").color(NamedTextColor.RED));
            }
        }
    }
}

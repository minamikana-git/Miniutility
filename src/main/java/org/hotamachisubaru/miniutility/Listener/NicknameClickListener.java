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
                // 「を」あり・なし両方を許容
        if (!title.equals("ニックネーム変更") && !title.equals("ニックネームを変更")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        switch (clickedItem.getType()) {
            case PAPER -> {
             player.sendMessage(Component.text("新しいニックネームをチャットで入力してください。").color(NamedTextColor.AQUA));
             // Chat クラスのフラグを使う
                Chat.setWaitingForNickname(player, true);
                player.closeInventory();
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

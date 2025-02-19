package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class NicknameChatInput implements Listener {

    private static final Set<Player> playersInNicknameInput = new HashSet<>();

    public static void startInput(Player player) {
        playersInNicknameInput.add(player);
    }

    @EventHandler
    public void NicknameInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playersInNicknameInput.contains(player)) {
            event.setCancelled(true);

            String nickname = event.getMessage().trim(); // トリムで余分な空白を除去
            if (nickname.isEmpty()) {
                player.sendMessage(Component.text("無効なニックネームです。空白にすることはできません。", NamedTextColor.RED));
                return;
            }

            if (nickname.length() > 16) {
                player.sendMessage(Component.text("ニックネームは16文字以内にしてください。", NamedTextColor.RED));
                return;
            }

            // データベースに保存
            NicknameDatabase.saveNickname(player.getUniqueId().toString(), nickname);

            // 表示名を更新
            NicknameManager.applyFormattedDisplayName(player);
            player.sendMessage(Component.text("ニックネームが設定されました: ", NamedTextColor.GREEN)
                    .append(Component.text(nickname, NamedTextColor.GREEN)));

            playersInNicknameInput.remove(player);
        }
    }
}

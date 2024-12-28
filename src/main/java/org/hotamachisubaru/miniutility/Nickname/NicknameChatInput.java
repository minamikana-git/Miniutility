package org.hotamachisubaru.miniutility.Nickname;
import org.bukkit.ChatColor;
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

            String nickname = event.getMessage();
            if (nickname.length() > 16) {
                player.sendMessage(ChatColor.RED + "ニックネームは16文字以内にしてください。");
                return;
            }

            // データベースに保存
            NicknameDatabase.saveNickname(player.getUniqueId().toString(), nickname);

            // 表示名を更新
            NicknameManager.applyFormattedDisplayName(player);
            player.sendMessage(ChatColor.GREEN + "ニックネームが設定されました: " + nickname);

            playersInNicknameInput.remove(player);
        }
    }
}

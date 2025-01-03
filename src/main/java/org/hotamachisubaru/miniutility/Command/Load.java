package org.hotamachisubaru.miniutility.Command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Listener.ChatListener;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;

public class Load implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String string,String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行出来ます。");
            return true;
        }

        Player player = (Player) sender;

        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
            player.sendMessage(ChatColor.YELLOW + "ニックネームが設定されていないため、プレイヤー名を使用します。");
        } else {
            player.sendMessage(ChatColor.GREEN + "データベースからニックネームを読み込みました。");

        }
        ChatListener.updateDisplayNameWithPrefix(player,nickname);
        return true;

    }
}

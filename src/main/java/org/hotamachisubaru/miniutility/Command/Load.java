package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Listener.Chat;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;

public class Load implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(NamedTextColor.RED + "このコマンドはプレイヤーのみ実行出来ます。"));
            return true;
        }

        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
            player.sendMessage(Component.text(NamedTextColor.YELLOW + "ニックネームが設定されていないため、プレイヤー名を使用します。"));
        } else {
            player.sendMessage(Component.text(NamedTextColor.GREEN + "データベースからニックネームを読み込みました。"));
        }
        Chat.updateDisplayNamePrefix(player, nickname);
        return true;
    }
}

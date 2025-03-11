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
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ実行出来ます。").color(NamedTextColor.RED));
            return true;
        }

        String nickname = NicknameDatabase.getNickname(player.getUniqueId().toString());
        if (nickname == null || nickname.isEmpty()) {
            nickname = player.getName();
            player.sendMessage(Component.text("ニックネームが設定されていないため、プレイヤー名を使用します。").color(NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("データベースからニックネームを読み込みました。").color(NamedTextColor.GREEN));
        }
        Chat.updateDisplayNamePrefix(player, nickname);
        return true;
    }
}

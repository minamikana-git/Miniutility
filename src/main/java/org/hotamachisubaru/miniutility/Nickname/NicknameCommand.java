package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Miniutility;

import java.sql.SQLException;

public class NicknameCommand implements CommandExecutor {
    private final Miniutility plugin;

    public NicknameCommand(Miniutility plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみが使用できます。", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(Component.text("新しいニックネームを入力してください。", NamedTextColor.YELLOW));
            try {
                NicknameManager.setNickname(player, "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        String nickname = String.join(" ", args).trim();
        try {
            NicknameManager.setNickname(player, nickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        player.displayName(Component.text(nickname));
        player.playerListName(Component.text(nickname));
        player.sendMessage(Component.text("ニックネームを " + nickname + " に設定しました！", NamedTextColor.GREEN));
        return true;
    }
}

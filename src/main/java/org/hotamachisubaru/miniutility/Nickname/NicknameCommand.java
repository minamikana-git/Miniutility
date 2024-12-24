package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみが使用できます。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "新しいニックネームを入力してください。");
            plugin.getNicknameConfig().setWaitingForNickname(player, true);
            return true;
        }

        String nickname = String.join(" ", args).trim();
        try {
            plugin.getNicknameConfig().setNickname(player.getUniqueId(), nickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', nickname));
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', nickname));
        player.sendMessage(ChatColor.GREEN + "ニックネームを " + nickname + " に設定しました！");
        return true;
    }
}

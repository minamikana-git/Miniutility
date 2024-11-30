package org.hotamachisubaru.miniutility.Nickname;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Miniutility;

import java.util.UUID;

public class NicknameCommand implements CommandExecutor {

    private final Miniutility plugin;

    public NicknameCommand(Miniutility plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ実行できます。"));
            return true;
        }

        UUID uuid = player.getUniqueId();
        NicknameConfig config = plugin.getNicknameConfig();

        if (args.length == 0) {
            // ニックネームのリセット
            config.setNickname(uuid, null);
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
            player.sendMessage(Component.text(ChatColor.GREEN + "ニックネームをリセットしました。"));
        } else {
            // ニックネームの設定
            String nickname = String.join(" ", args);
            config.setNickname(uuid, nickname);
            player.displayName(Component.text(nickname));
            player.playerListName(Component.text(nickname));
            player.sendMessage(Component.text(ChatColor.GREEN + "ニックネームを " + nickname + " に設定しました。"));
        }

        return true; // コマンド処理を成功として返す
    }
}

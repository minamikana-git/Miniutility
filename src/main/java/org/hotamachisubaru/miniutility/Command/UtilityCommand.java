package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.Miniutility;

public class UtilityCommand implements CommandExecutor {

    private final Miniutility plugin;

    public UtilityCommand(Miniutility plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ使用できます。"));
            return true;
        }
        // ユーティリティメニューを開く
        GUI.openMenu(player,plugin);
        return true;
    }
}

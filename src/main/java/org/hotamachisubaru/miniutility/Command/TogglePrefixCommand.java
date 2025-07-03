package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.Miniutility;

public class TogglePrefixCommand implements CommandExecutor {

    private final Miniutility plugin;

    public TogglePrefixCommand(Miniutility plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("プレイヤーのみ実行可能です。");
            return true;
        }
        // プレフィックスのON/OFF切り替え例
        var manager = plugin.getNicknameManager();
        boolean enabled = manager.togglePrefix(player.getUniqueId());
        player.sendMessage(Component.text("Prefixの表示が " + (enabled ? "有効" : "無効") + " になりました。"));
        return true;
    }
}

package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

public class LoadCommand implements CommandExecutor {

    private final MiniutilityLoader plugin;

    public LoadCommand(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 例：データ再読み込み処理
        plugin.getMiniutility().getNicknameDatabase().reload();
        sender.sendMessage(Component.text("ニックネームデータを再読み込みしました。").color(NamedTextColor.GREEN));
        return true;
    }
}

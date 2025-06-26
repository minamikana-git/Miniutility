package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * "combine-prefix" の ON/OFF を切り替えるコマンド実装クラス
 * コマンド例: /prefixtoggle
 */
public class TogglePrefixCommand implements CommandExecutor {
    private final Plugin plugin;

    public TogglePrefixCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // プレイヤー専用コマンドにしたい場合
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ使用できます。").color(NamedTextColor.RED));
            return true;
        }

        // 必要に応じて権限チェックを行ってください
        if (!sender.hasPermission("miniutility.toggleprefix")) {
            sender.sendMessage(Component.text("権限がありません。").color(NamedTextColor.RED));
            return true;
        }

        // 現在の combine-prefix 値を読み込み、反転させる
        boolean current = plugin.getConfig().getBoolean("combine-prefix", true);
        boolean next = !current;
        plugin.getConfig().set("combine-prefix", next);
        plugin.saveConfig();

        String status = next ? "ON" : "OFF";
        sender.sendMessage(Component.text("Prefix とニックネームの結合を " + status + " にしました。").color(NamedTextColor.GREEN));
        return true;
    }
}

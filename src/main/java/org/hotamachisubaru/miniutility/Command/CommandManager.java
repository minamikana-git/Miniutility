package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.MiniutilityLoader;
import org.hotamachisubaru.miniutility.Nickname.NicknameDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final MiniutilityLoader plugin;

    public CommandManager(MiniutilityLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "menu" -> {
                if (sender instanceof Player player) {
                    GUI.openMenu(player);
                } else {
                    sender.sendMessage("プレイヤーのみ使用できます。");
                }
                return true;
            }
            case "load" -> {
                // NicknameDatabaseにreloadが無ければ、データの再初期化や再ロードに相当する処理を呼ぶ
                try {
                    NicknameDatabase.reload();
                    sender.sendMessage("ニックネームデータを再読み込みしました。");
                } catch (Throwable e) {
                    sender.sendMessage(Component.text("データベース再読み込みに失敗しました: " + e.getMessage()).color(NamedTextColor.RED));
                }
                return true;
            }
            case "prefixtoggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("プレイヤーのみ実行可能です。");
                    return true;
                }
                var manager = plugin.getMiniutility().getNicknameManager();
                // togglePrefixがbooleanを返す想定
                boolean enabled;
                try {
                    enabled = manager.togglePrefix(player.getUniqueId());
                } catch (Throwable e) {
                    sender.sendMessage(Component.text("Prefixの切り替えに失敗しました: " + e.getMessage()).color(NamedTextColor.RED));
                    return true;
                }
                player.sendMessage(Component.text("Prefixの表示が " + (enabled ? "有効" : "無効") + " になりました。").color(NamedTextColor.GREEN));
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("不明なコマンドです。").color(NamedTextColor.RED));
                return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("prefixtoggle")) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>();
                options.add("on");
                options.add("off");
                if (!args[0].isEmpty()) {
                    options.removeIf(opt -> !opt.startsWith(args[0].toLowerCase()));
                }
                return options;
            }
        }
        return Collections.emptyList();
    }
}

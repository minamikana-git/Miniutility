package org.hotamachisubaru.miniutility.Command;

import org.bukkit.ChatColor;
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
        String name = command.getName().toLowerCase();

        switch (name) {
            case "menu":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    GUI.openMenu(player);
                } else {
                    sender.sendMessage("プレイヤーのみ使用できます。");
                }
                return true;

            case "load":
                try {
                    NicknameDatabase.reload();
                    sender.sendMessage(ChatColor.GREEN + "ニックネームデータを再読み込みしました。");
                } catch (Throwable e) {
                    sender.sendMessage(ChatColor.RED + "データベース再読み込みに失敗しました: " + e.getMessage());
                }
                return true;

            case "prefixtoggle":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("プレイヤーのみ実行可能です。");
                    return true;
                }
                Player p = (Player) sender;
                boolean enabled;
                try {
                    enabled = plugin.getMiniutility().getNicknameManager().togglePrefix(p.getUniqueId());
                } catch (Throwable e) {
                    sender.sendMessage(ChatColor.RED + "Prefixの切り替えに失敗しました: " + e.getMessage());
                    return true;
                }
                p.sendMessage(ChatColor.GREEN + "Prefixの表示が " + (enabled ? "有効" : "無効") + " になりました。");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "不明なコマンドです。");
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if ("prefixtoggle".equalsIgnoreCase(command.getName())) {
            if (args.length == 1) {
                List<String> options = new ArrayList<String>();
                options.add("on");
                options.add("off");
                if (args[0] != null && !args[0].isEmpty()) {
                    String head = args[0].toLowerCase();
                    List<String> filtered = new ArrayList<String>();
                    for (String o : options) if (o.startsWith(head)) filtered.add(o);
                    return filtered;
                }
                return options;
            }
        }
        return Collections.emptyList();
    }
}

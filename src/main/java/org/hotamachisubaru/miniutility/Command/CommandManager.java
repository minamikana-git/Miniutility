package org.hotamachisubaru.miniutility.Command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
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
                    GUI.openMenu((Player) sender);
                } else {
                    TextComponent component = new TextComponent();
                    component.setText("プレイヤーのみ使用できます。");
                    component.setColor(ChatColor.RED);
                    sender.sendMessage(component);
                }
                return true;

            case "load":
                try {
                    NicknameDatabase.reload();

                    TextComponent component = new TextComponent();
                    component.setText("ニックネームデータを再読み込みしました。");
                    component.setColor(ChatColor.GREEN);

                    sender.sendMessage(component);
                } catch (Throwable e) {
                    TextComponent component = new TextComponent();
                    component.setText("データベース再読み込みに失敗しました: " + e.getMessage());
                    component.setColor(ChatColor.RED);

                    sender.sendMessage(component);
                }
                return true;

            case "prefixtoggle":
                if (!(sender instanceof Player)) {
                    TextComponent component = new TextComponent();
                    component.setText("プレイヤーのみ実行可能です。");
                    component.setColor(ChatColor.RED);

                    sender.sendMessage(component);
                    return true;
                }
                Player player = (Player) sender;
                boolean enabled;
                try {
                    enabled = plugin.getMiniutility().getNicknameManager().togglePrefix(player.getUniqueId());
                } catch (Throwable e) {
                    TextComponent component = new TextComponent();
                    component.setText("Prefixの切り替えに失敗しました: " + e.getMessage());
                    component.setColor(ChatColor.RED);

                    sender.sendMessage(component);
                    return true;
                }

                TextComponent component = new TextComponent();
                component.setText("Prefixの表示が " + (enabled ? "有効" : "無効") + " になりました。");
                component.setColor(ChatColor.GREEN);

                player.sendMessage(component);
                return true;

            default:
                TextComponent componentp = new TextComponent();
                componentp.setText("不明なコマンドです。");
                componentp.setColor(ChatColor.RED);

                sender.sendMessage(componentp);
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

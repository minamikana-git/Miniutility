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
        // コマンド名で分岐（コマンドはplugin.yml/paper-plugin.ymlで必ず登録する）
        switch (command.getName().toLowerCase()) {
            case "menu" -> {
                if (sender instanceof Player player) {
                    GUI.openMenu(player, plugin);
                } else {
                    sender.sendMessage("プレイヤーのみ使用できます。");
                }
                return true;
            }
            case "load" -> {
                plugin.getMiniutility().getNicknameDatabase().reload();
                sender.sendMessage("ニックネームデータを再読み込みしました。");
                return true;
            }
            case "prefixtoggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("プレイヤーのみ実行可能です。");
                    return true;
                }
                var manager = plugin.getMiniutility().getNicknameManager();
                boolean enabled = manager.togglePrefix(player.getUniqueId());
                player.sendMessage(Component.text("Prefixの表示が " + (enabled ? "有効" : "無効") + " になりました。").color(NamedTextColor.GREEN));
                return true;
            }
            // ここに他のコマンドも追加可能
            default -> {
                sender.sendMessage(Component.text("不明なコマンドです。").color(NamedTextColor.RED));
                return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // 例：/menu なら何も補完なし、/loadなら何もなし、/prefixtoggleだけ「on/off」補完
        if (command.getName().equalsIgnoreCase("prefixtoggle")) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>();
                options.add("on");
                options.add("off");
                // 入力内容でフィルタ
                if (!args[0].isEmpty()) {
                    options.removeIf(opt -> !opt.startsWith(args[0].toLowerCase()));
                }
                return options;
            }
        }
        // 他コマンドも必要なら追加
        return Collections.emptyList();
    }
}

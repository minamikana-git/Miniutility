package org.hotamachisubaru.miniutility.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hotamachisubaru.miniutility.GUI.GUI;
import org.hotamachisubaru.miniutility.MiniutilityLoader;

public class CommandManager {

    public static void registerCommands(MiniutilityLoader plugin) {
        // /menu
        plugin.getServer().getCommandMap().register("menu", new Command("menu") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                if (sender instanceof Player player) {
                    GUI.openMenu(player, plugin);
                } else {
                    sender.sendMessage("プレイヤーのみ使用できます。");
                }
                return true;
            }
        });

        // /load
        plugin.getServer().getCommandMap().register("load", new Command("load") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                plugin.getMiniutility().getNicknameDatabase().reload();
                sender.sendMessage("ニックネームデータを再読み込みしました。");
                return true;
            }
        });

        // /prefixtoggle
        plugin.getServer().getCommandMap().register("prefixtoggle", new Command("prefixtoggle") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("プレイヤーのみ実行可能です。");
                    return true;
                }
                var manager = plugin.getMiniutility().getNicknameManager();
                boolean enabled = manager.togglePrefix(player.getUniqueId());
                player.sendMessage(Component.text("Prefixの表示が " + (enabled ? "有効" : "無効") + " になりました。").color(NamedTextColor.GREEN));
                return true;
            }
        });

        // /doublejumptoggle
        plugin.getServer().getCommandMap().register("doublejumptoggle", new Command("doublejumptoggle") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("プレイヤーのみ実行可能です。");
                    return true;
                }
                var doubleJumpListener = plugin.getMiniutility().getDoubleJumpListener();
                boolean enabled = doubleJumpListener.toggleDoubleJump(player.getUniqueId(), player);
                player.sendMessage(Component.text("2段ジャンプが " + (enabled ? "有効" : "無効") + " になりました。").color(NamedTextColor.GREEN));
                return true;
            }
        });
    }
}

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.hotamachisubaru.miniutility.Miniutility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Nickname implements Listener {
    private final Map<UUID, Boolean> waitingForNickname = new HashMap<>();
    private final Miniutility plugin;

    public Nickname(Miniutility plugin) {
        this.plugin = plugin;
    }

    public void setWaitingForNickname(Player player, boolean waiting) {
        waitingForNickname.put(player.getUniqueId(), waiting);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (waitingForNickname.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

            if (message.isEmpty()) {
                player.sendMessage(ChatColor.RED + "無効なニックネームです。もう一度入力してください。");
                return;
            }

            plugin.getNicknameConfig().setNickname(playerUUID, message);
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', message));
            player.sendMessage(ChatColor.GREEN + "ニックネームを " + message + " に設定しました！");
            waitingForNickname.remove(playerUUID);
        }
    }
}

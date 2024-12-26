package org.hotamachisubaru.miniutility.Nickname;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.logging.Logger;

public class DisplayNameUtil {

    public static void applyFormattedDisplayName(Player player) {
        CachedMetaData metaData = LuckPermsProvider.get()
                .getPlayerAdapter(Player.class)
                .getMetaData(player);

        // LuckPermsのPrefixを取得
        String prefix = metaData.getPrefix();
        if (prefix == null) {
            prefix = ""; // プレフィックスがnullの場合は空文字
        }

        // データベースからニックネームを取得
        String nickname = NicknameDatabase.loadNicknameFromDatabase(player.getUniqueId().toString());
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = player.getName(); // ニックネームがない場合はプレイヤー名
        }

        // Prefixとニックネームを結合
        String formattedName = ChatColor.translateAlternateColorCodes('&', prefix + " " + nickname);

        // DisplayNameとPlayerListNameを設定
        player.setDisplayName(formattedName);
        player.setPlayerListName(formattedName);

        Logger.getLogger("Miniutility").info("プレイヤー " + player.getName() + " のフォーマット済み表示名: " + formattedName);
    }
}

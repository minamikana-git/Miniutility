package org.hotamachisubaru.miniutility.Nickname;

import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NicknameConfig {
    private final Map<UUID, Boolean> waitingForNicknameInput = new HashMap<>();

    public void setNickname(UUID uuid, String nickname) throws SQLException {
        NicknameDatabase.saveNicknameToDatabase(uuid.toString(), nickname);
    }

    public String getNickname(UUID uuid, String defaultName) throws SQLException {
        String nickname = NicknameDatabase.loadNicknameFromDatabase(uuid.toString());
        return (nickname != null) ? nickname : defaultName;
    }

    public void setWaitingForNickname(Player player, boolean b) {
        waitingForNicknameInput.put(player.getUniqueId(), b);
    }

    public boolean isWaitingForNickname(Player player) {
        return waitingForNicknameInput.getOrDefault(player.getUniqueId(), false);
    }
}

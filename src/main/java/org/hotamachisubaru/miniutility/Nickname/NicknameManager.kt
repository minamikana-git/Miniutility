package org.hotamachisubaru.miniutility.Nickname

import net.luckperms.api.LuckPermsProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Logger
import java.util.regex.Pattern

class NicknameManager(private val nicknameConfig: NicknameConfig) : Listener {

    private val logger: Logger = Logger.getLogger("Miniutility")

    @EventHandler
    fun loadNickname(event: PlayerJoinEvent) {
        val player = event.player
        logger.info("Player joined: ${player.name}")
        applyFormattedDisplayName(player)
    }

    fun setNickname(player: Player, nickname: String): String {
        require(nickname.trim().isNotEmpty()) { "無効なニックネームです。空白にすることはできません。" }
        require(nickname.length <= 16) { "ニックネームは16文字以内にしてください。" }

        nicknameConfig.setNickname(player.uniqueId, nickname)
        logger.info("Setting nickname for player ${player.name}: $nickname")

        applyFormattedDisplayName(player)
        player.sendMessage("${ChatColor.GREEN}ニックネームが設定されました: $nickname")
        return nickname
    }

    private fun translateHexColorCodes(message: String): String {
        val hexPattern = Pattern.compile("(?i)&#([0-9a-fA-F]{6})")
        val matcher = hexPattern.matcher(message)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val replacement = matcher.group(1).toCharArray().joinToString("") { "§$it" }
            matcher.appendReplacement(buffer, "§x$replacement")
        }
        matcher.appendTail(buffer)
        return buffer.toString()
    }

    fun applyFormattedDisplayName(player: Player) {
        val luckPerms = LuckPermsProvider.get()
        val metaData = luckPerms.getPlayerAdapter(Player::class.java).getMetaData(player)

        val prefix = metaData.prefix ?: ""
        val nickname = nicknameConfig.getNickname(player.uniqueId, player.name)

        val formattedName = translateHexColorCodes(prefix + nickname)

        player.setDisplayName(formattedName)
        player.setPlayerListName(formattedName)

        logger.info("Formatted display name for player ${player.name}: $formattedName")
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        event.format = "${player.displayName}: ${event.message}"
    }
}


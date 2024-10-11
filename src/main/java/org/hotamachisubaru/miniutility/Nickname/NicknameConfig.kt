package org.hotamachisubaru.miniutility.Nickname

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.*


class NicknameConfig(plugin: JavaPlugin) {
    private val config: FileConfiguration
    private val configFile = File(plugin.dataFolder, "nickname.yml")

    init {
        if (!configFile.exists()) {
            plugin.saveResource("nickname.yml", false)
        }
        this.config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun getNickname(uuid: UUID): String {
        return config.getString("nicknames.$uuid", null)!!
    }

    fun setNickname(uuid: UUID, nickname: String?) {
        config["nicknames.$uuid"] = nickname
        saveConfig()
    }

    private fun saveConfig() {
        try {
            config.save(configFile)
        } catch (e: IOException) {
           error("設定ファイルの保存に失敗しました。")
        }
    }
}



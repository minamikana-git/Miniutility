package org.hotamachisubaru.miniutility.Nickname

import org.bukkit.ChatColor
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Logger

class NicknameDatabase(path: String) {
    private var connection: Connection? = null

    init {
        Companion.path = Paths.get(path).toAbsolutePath().toString()
    }

    fun setupDatabase() {
        try {
            openConnection() // データベース接続を開く
            createTables() // 必要なテーブルを作成
            logger.info("データベースのセットアップが正常に完了しました。")
        } catch (e: SQLException) {
            logger.severe("データベースのセットアップに失敗しました: " + e.message)
            e.printStackTrace()
        }
    }

    @Throws(SQLException::class)
    private fun createTables() {
        if (connection != null) {
            connection!!.createStatement().use { statement ->
                val nicknamesTableQuery = """
                        CREATE TABLE IF NOT EXISTS nicknames (
                            uuid TEXT PRIMARY KEY,
                            nickname TEXT
                        );
                        
                        """.trimIndent()
                statement.execute(nicknamesTableQuery)
                logger.info("ニックネームテーブルを作成しました（既に存在する場合もあります）。")
            }
        }
    }

    @Throws(SQLException::class)
    fun openConnection() {
        val url = "jdbc:sqlite:" + path + "/nickname.db" // URL修正
        connection = DriverManager.getConnection(url)
        createTables()
    }

    fun closeConnection() {
        try {
            if (connection != null && !connection!!.isClosed) {
                connection!!.close()
            }
        } catch (e: SQLException) {
            logger.severe("データベース接続を閉じることができませんでした: " + e.message)
        }
    }

    companion object {
        private const val DB_URL = "jdbc:sqlite:plugins/Miniutility/nickname.db"
        private var path: String? = null
        private val logger: Logger = Logger.getLogger("Database")

        init {
            try {
                DriverManager.getConnection(DB_URL).use { conn ->
                    val createTable = "CREATE TABLE IF NOT EXISTS nicknames (uuid TEXT PRIMARY KEY, nickname TEXT);"
                    conn.createStatement().execute(createTable)
                }
            } catch (e: SQLException) {
                logger.severe("データベースの接続に失敗しました。")
            }
        }

        @JvmStatic
        fun saveNickname(uuid: String?, nickname: String) {
            val formattedNickname = ChatColor.translateAlternateColorCodes('&', nickname) // カラーコードを適用
            try {
                DriverManager.getConnection(DB_URL).use { conn ->
                    conn.prepareStatement("REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?);").use { stmt ->
                        stmt.setString(1, uuid)
                        stmt.setString(2, formattedNickname)
                        stmt.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                logger.severe("ニックネームの保存に失敗しました。")
            }
        }


        @JvmStatic
        fun getNickname(uuid: String?): String? {
            try {
                DriverManager.getConnection(DB_URL).use { conn ->
                    conn.prepareStatement("SELECT nickname FROM nicknames WHERE uuid = ?;").use { stmt ->
                        stmt.setString(1, uuid)
                        val rs = stmt.executeQuery()
                        if (rs.next()) {
                            return rs.getString("nickname")
                        }
                    }
                }
            } catch (e: SQLException) {
                logger.warning("ニックネームの取得に失敗しました。ファイルが破損している可能性があります。")
            }
            return null
        }
    }
}



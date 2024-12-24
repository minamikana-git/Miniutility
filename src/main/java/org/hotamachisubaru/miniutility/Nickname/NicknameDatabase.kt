package org.hotamachisubaru.miniutility.Nickname

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Logger

class NicknameDatabase(private val path: String) {
    private var connection: Connection? = null

    companion object {
        private val logger: Logger = Logger.getLogger("NicknameDatabase")

        @JvmStatic
        @Throws(SQLException::class)
        fun saveNicknameToDatabase(uuid: String, nickname: String) {
            val query = "REPLACE INTO nicknames (uuid, nickname) VALUES (?, ?);"
            try {
                DriverManager.getConnection("jdbc:sqlite:nickname.db").use { conn ->
                    conn.prepareStatement(query).use { pstmt ->
                        pstmt.setString(1, uuid)
                        pstmt.setString(2, nickname)
                        pstmt.executeUpdate()
                        logger.info("Nickname saved to database for UUID: $uuid")
                    }
                }
            } catch (e: SQLException) {
                logger.severe("Failed to save nickname to database: ${e.message}")
            }
        }

        @JvmStatic
        @Throws(SQLException::class)
        fun loadNicknameFromDatabase(uuid: String): String? {
            val query = "SELECT nickname FROM nicknames WHERE uuid = ?;"
            try {
                DriverManager.getConnection("jdbc:sqlite:nickname.db").use { conn ->
                    conn.prepareStatement(query).use { pstmt ->
                        pstmt.setString(1, uuid)
                        pstmt.executeQuery().use { rs ->
                            if (rs.next()) {
                                return rs.getString("nickname")
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                logger.severe("Failed to retrieve nickname from database: ${e.message}")
            }
            return null
        }
    }
    @Throws(SQLException::class)
    fun openConnection(path: String) {
        val url = "jdbc:sqlite:$path/nickname.db"
        connection = DriverManager.getConnection(url)
        createTables()
    }


    @Throws(SQLException::class)
    private fun createTables() {
        connection?.createStatement()?.use { statement ->
            val nicknamesTableQuery = "CREATE TABLE IF NOT EXISTS nicknames (uuid TEXT PRIMARY KEY, nickname TEXT);"
            statement.executeUpdate(nicknamesTableQuery)
            logger.info("Database tables initialized successfully.")
        } ?: throw SQLException("Database connection is not established.")
    }

    fun closeConnection() {
        try {
            connection?.close()
            logger.info("Database connection closed.")
        } catch (e: SQLException) {
            logger.severe("Failed to close the database connection: ${e.message}")
        }
    }

    fun setupDatabase() {
        try {
            openConnection("./plugins/Miniutility")
            logger.info("Database setup completed successfully.")
        } catch (e: SQLException) {
            logger.severe("Failed to setup the database: ${e.message}")

        }
    }
}
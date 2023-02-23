package love.chihuyu.database

import org.jetbrains.exposed.sql.Table

object NameRecord : Table("nameRecord") {
    val uuid = uuid("uuid").uniqueIndex()
    val ign = text("ign")
}

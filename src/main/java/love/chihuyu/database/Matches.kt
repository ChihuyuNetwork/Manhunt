package love.chihuyu.database

import love.chihuyu.game.Teams
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Matches: Table("matches") {
    val date = datetime("datetime").uniqueIndex()
    val matchTime = long("matchTime")
    val winnerTeam = enumeration<Teams>("winnerTeam")

    override val primaryKey: PrimaryKey = PrimaryKey(date)
}
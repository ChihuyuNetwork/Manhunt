package love.chihuyu.database

import love.chihuyu.game.ManhuntMission
import love.chihuyu.game.Teams
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Matches : Table("matches") {
    val date = datetime("datetime").uniqueIndex()
    val seed = long("seed")
    val matchTime = long("matchTime")
    val winnerTeam = enumeration<Teams>("winnerTeam")
    val mission = enumeration<ManhuntMission>("mission").default(ManhuntMission.ENTER_END_PORTAL)

    override val primaryKey: PrimaryKey = PrimaryKey(date)
}

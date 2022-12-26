package love.chihuyu.database

import love.chihuyu.game.Teams
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Users : IntIdTable("users") {

    val uuid = uuid("uuid")
    val date = datetime("datetime")
    val team = enumeration("team", Teams::class)

    // 移動
    val aliveTime = long("aliveTime")
    val timeToTheEnd = long("timeToTheEnd")
    val timeToNether = long("timeToNether")
    val traveled = long("traveled")

    // PvP
    val deathes = integer("deathes")
    val playersKilled = integer("playersKilled")
    val arrowShooted = integer("arrowShooted")
    val arrowHitted = integer("arrowHitted")

    // モブ
    val mobsKilled = integer("mobsKilled")
    val endermansKilled = integer("endermansKilled")
    val blazesKilled = integer("blazesKilled")

    // ブロック
    val blocksBroken = integer("blocksBroken")
    val blocksPlaced = integer("blocksPlaced")

    // アイテム
    val itemsCrafted = integer("itemsCrafted")
    val potionsUsed = integer("potionsUsed")
    val gapplesUsed = integer("gapplesUsed")
    val itemsEnchanted = integer("itemsEnchanted")
    val toolsRepaired = integer("toolsRepaired")

    // 準備
    val ironsSmelt = integer("ironsSmelt")
    val coalsMined = integer("coalsMined")
    val itemsTraded = integer("itemsTraded")
    val itemsBartered = integer("itemsBartered")

    // ワールド
    val openedLoots = integer("openedLoots")
    val chats = integer("chats")

    init {
        uniqueIndex(uuid, date)
    }
}

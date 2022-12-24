package love.chihuyu.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Hunters: IntIdTable("hunters") {

    val uuid = uuid("uuid")
    val date = datetime("datetime")

    val deathes = integer("deathes")
    val runnersKilled = integer("runnersKilled")

    // Global

    val victoried = bool("victoried")
    val aliveTime = long("aliveTime")
    val timeToTheEnd = long("timeToTheEnd")
    val timeToNether = long("timeToNether")

    val mobsKilled = integer("mobsKilled")
    val endermansKilled = integer("endermansKilled")
    val blazesKilled = integer("blazesKilled")

    val blocksBroken = integer("blocksBroken")
    val blocksPlaced = integer("blocksPlaced")
    val traveled = long("traveled")

    val itemsTraded = integer("itemsTraded")
    val itemsBartered = integer("itemsBartered")
    val openedLoots = integer("openedLoots")

    val foodsCrafted = integer("foodsCrafted")
    val ironsSmelt = integer("ironsSmelt")
    val coalsMined = integer("coalsMined")
    val potionsBrewed = integer("potionsBrewed")
    val itemsCrafted = integer("itemsCrafted")
    val itemsEnchanted = integer("itemsEnchanted")
    val toolsRepaired = integer("toolsRepaired")

    val arrowShooted = integer("arrowShooted")
    val arrowHitted = integer("arrowHitted")

    init {
        uniqueIndex(uuid, date)
    }
}
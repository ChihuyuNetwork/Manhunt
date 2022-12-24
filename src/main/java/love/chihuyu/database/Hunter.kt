package love.chihuyu.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class Hunter(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Hunter>(Hunters) {
        inline fun findOrNew(uuid: UUID, datetime: LocalDateTime, crossinline init: Hunter.() -> Unit = {}) =
            find(uuid, datetime) ?: new {
                this.uuid = uuid
                transaction {
                    init()
                }
            }

        fun find(uuid: UUID, datetime: LocalDateTime) =
            transaction { find { (Hunters.uuid eq uuid) and (Hunters.date eq datetime) }.limit(1).firstOrNull() }
    }

    var uuid by Hunters.uuid
    var date by Hunters.date

    var deathes by Hunters.deathes
    var runnersKilled by Hunters.runnersKilled

    var victoried by Hunters.victoried
    var aliveTime by Hunters.aliveTime
    var timeToTheEnd by Hunters.timeToTheEnd
    var timeToNether by Hunters.timeToNether

    var mobsKilled by Hunters.mobsKilled
    var endermansKilled by Hunters.endermansKilled
    var blazesKilled by Hunters.blazesKilled

    var blocksBroken by Hunters.blocksBroken
    var blocksPlaced by Hunters.blocksPlaced
    var traveled by Hunters.traveled

    var itemsTraded by Hunters.itemsTraded
    var itemsBartered by Hunters.itemsBartered
    var openedLoots by Hunters.openedLoots

    var foodsCrafted by Hunters.foodsCrafted
    var ironsSmelt by Hunters.ironsSmelt
    var coalsMined by Hunters.coalsMined
    var potionsBrewed by Hunters.potionsBrewed
    var itemsCrafted by Hunters.itemsCrafted
    var itemsEnchanted by Hunters.itemsEnchanted
    var toolsRepaired by Hunters.toolsRepaired

    var arrowShooted by Hunters.arrowShooted
    var arrowHitted by Hunters.arrowHitted
}
package love.chihuyu.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class Runner(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Runner>(Runners) {
        inline fun findOrNew(uuid: UUID, datetime: LocalDateTime, crossinline init: Runner.() -> Unit = {}) =
            find(uuid, datetime) ?: new {
                this.uuid = uuid
                transaction {
                    init()
                }
            }

        fun find(uuid: UUID, datetime: LocalDateTime) =
            transaction { find { (Runners.uuid eq uuid) and (Runners.date eq datetime) }.limit(1).firstOrNull() }
    }

    var uuid by Runners.uuid
    var date by Runners.date

    var huntersKilled by Runners.huntersKilled

    var victoried by Runners.victoried
    var aliveTime by Runners.aliveTime
    var timeToTheEnd by Runners.timeToTheEnd
    var timeToNether by Runners.timeToNether

    var mobsKilled by Runners.mobsKilled
    var endermansKilled by Runners.endermansKilled
    var blazesKilled by Runners.blazesKilled

    var blocksBroken by Runners.blocksBroken
    var blocksPlaced by Runners.blocksPlaced
    var traveled by Runners.traveled

    var itemsTraded by Runners.itemsTraded
    var itemsBartered by Runners.itemsBartered
    var openedLoots by Runners.openedLoots

    var foodsCrafted by Runners.foodsCrafted
    var ironsSmelt by Runners.ironsSmelt
    var coalsMined by Runners.coalsMined
    var potionsBrewed by Runners.potionsBrewed
    var itemsCrafted by Runners.itemsCrafted
    var itemsEnchanted by Runners.itemsEnchanted
    var toolsRepaired by Runners.toolsRepaired

    var arrowShooted by Runners.arrowShooted
    var arrowHitted by Runners.arrowHitted
}
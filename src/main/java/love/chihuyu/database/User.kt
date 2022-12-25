package love.chihuyu.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users) {
        inline fun findOrNew(uuid: UUID, datetime: LocalDateTime, crossinline init: User.() -> Unit = {}) =
            find(uuid, datetime) ?: new {
                this.uuid = uuid
                this.date = datetime
                transaction {
                    init()
                }
            }

        fun find(uuid: UUID, datetime: LocalDateTime) =
            transaction { find { (Users.uuid eq uuid) and (Users.date eq datetime) }.limit(1).firstOrNull() }
    }

    var uuid by Users.uuid
    var date by Users.date

    var deathes by Users.deathes

    var team by Users.team
    var aliveTime by Users.aliveTime
    var timeToTheEnd by Users.timeToTheEnd
    var timeToNether by Users.timeToNether

    var playersKilled by Users.playersKilled
    var mobsKilled by Users.mobsKilled
    var endermansKilled by Users.endermansKilled
    var blazesKilled by Users.blazesKilled

    var blocksBroken by Users.blocksBroken
    var blocksPlaced by Users.blocksPlaced
    var traveled by Users.traveled

    var itemsTraded by Users.itemsTraded
    var itemsBartered by Users.itemsBartered
    var openedLoots by Users.openedLoots

    var foodsCrafted by Users.foodsCrafted
    var ironsSmelt by Users.ironsSmelt
    var coalsMined by Users.coalsMined
    var potionsBrewed by Users.potionsBrewed
    var itemsCrafted by Users.itemsCrafted
    var itemsEnchanted by Users.itemsEnchanted
    var toolsRepaired by Users.toolsRepaired

    var arrowShooted by Users.arrowShooted
    var arrowHitted by Users.arrowHitted
}
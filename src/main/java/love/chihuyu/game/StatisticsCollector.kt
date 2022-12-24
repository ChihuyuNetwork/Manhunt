package love.chihuyu.game

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.database.Hunter
import love.chihuyu.database.Runner
import love.chihuyu.utils.TeamUtil.isHunter
import love.chihuyu.utils.TeamUtil.isRunner
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.purpurmc.purpur.event.inventory.AnvilTakeResultEvent
import java.time.Instant
import java.time.LocalDateTime

object StatisticsCollector : Listener {
    private val temporaryRecord = hashMapOf<StatisticsType, MutableMap<OfflinePlayer, Any>>()
    private val locationCache = mutableMapOf<OfflinePlayer, Location>()

    fun collect(
        startTime: LocalDateTime
    ) {
        plugin.server.onlinePlayers.forEach { player ->
            if (player.isHunter()) {
                Hunter.findOrNew(player.uniqueId, startTime) {
                    runCatching {
                        this.deathes = temporaryRecord[StatisticsType.deathes]!![player] as Int
                        this.runnersKilled = temporaryRecord[StatisticsType.runnersKilled]!![player] as Int

                        this.victoried = temporaryRecord[StatisticsType.victoried]!![player] as Boolean
                        this.aliveTime = temporaryRecord[StatisticsType.aliveTime]!![player] as Long
                        this.timeToTheEnd = temporaryRecord[StatisticsType.timeToTheEnd]!![player] as Long
                        this.timeToNether = temporaryRecord[StatisticsType.timeToNether]!![player] as Long
                        this.mobsKilled = temporaryRecord[StatisticsType.mobsKilled]!![player] as Int
                        this.endermansKilled = temporaryRecord[StatisticsType.endermansKilled]!![player] as Int
                        this.blazesKilled = temporaryRecord[StatisticsType.blazesKilled]!![player] as Int
                        this.blocksBroken = temporaryRecord[StatisticsType.blocksBroken]!![player] as Int
                        this.blocksPlaced = temporaryRecord[StatisticsType.blocksPlaced]!![player] as Int
                        this.traveled = temporaryRecord[StatisticsType.traveled]!![player] as Long
                        this.itemsTraded = temporaryRecord[StatisticsType.itemsTraded]!![player] as Int
                        this.itemsBartered = temporaryRecord[StatisticsType.itemsBartered]!![player] as Int
                        this.openedLoots = temporaryRecord[StatisticsType.openedLoots]!![player] as Int
                        this.foodsCrafted = temporaryRecord[StatisticsType.foodsCrafted]!![player] as Int
                        this.ironsSmelt = temporaryRecord[StatisticsType.ironsSmelt]!![player] as Int
                        this.coalsMined = temporaryRecord[StatisticsType.coalsMined]!![player] as Int
                        this.potionsBrewed = temporaryRecord[StatisticsType.potionsBrewed]!![player] as Int
                        this.itemsCrafted = temporaryRecord[StatisticsType.itemsCrafted]!![player] as Int
                        this.itemsEnchanted = temporaryRecord[StatisticsType.itemsEnchanted]!![player] as Int
                        this.toolsRepaired = temporaryRecord[StatisticsType.toolsRepaired]!![player] as Int
                        this.arrowShooted = temporaryRecord[StatisticsType.arrowShooted]!![player] as Int
                        this.arrowHitted = temporaryRecord[StatisticsType.arrowHitted]!![player] as Int
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            } else if (player.isRunner()) {
                Runner.findOrNew(player.uniqueId, startTime) {
                    runCatching {
                        this.huntersKilled = temporaryRecord[StatisticsType.huntersKilled]!![player] as Int

                        this.victoried = temporaryRecord[StatisticsType.victoried]!![player] as Boolean
                        this.aliveTime = temporaryRecord[StatisticsType.aliveTime]!![player] as Long
                        this.timeToTheEnd = temporaryRecord[StatisticsType.timeToTheEnd]!![player] as Long
                        this.timeToNether = temporaryRecord[StatisticsType.timeToNether]!![player] as Long
                        this.mobsKilled = temporaryRecord[StatisticsType.mobsKilled]!![player] as Int
                        this.endermansKilled = temporaryRecord[StatisticsType.endermansKilled]!![player] as Int
                        this.blazesKilled = temporaryRecord[StatisticsType.blazesKilled]!![player] as Int
                        this.blocksBroken = temporaryRecord[StatisticsType.blocksBroken]!![player] as Int
                        this.blocksPlaced = temporaryRecord[StatisticsType.blocksPlaced]!![player] as Int
                        this.traveled = temporaryRecord[StatisticsType.traveled]!![player] as Long
                        this.itemsTraded = temporaryRecord[StatisticsType.itemsTraded]!![player] as Int
                        this.itemsBartered = temporaryRecord[StatisticsType.itemsBartered]!![player] as Int
                        this.openedLoots = temporaryRecord[StatisticsType.openedLoots]!![player] as Int
                        this.foodsCrafted = temporaryRecord[StatisticsType.foodsCrafted]!![player] as Int
                        this.ironsSmelt = temporaryRecord[StatisticsType.ironsSmelt]!![player] as Int
                        this.coalsMined = temporaryRecord[StatisticsType.coalsMined]!![player] as Int
                        this.potionsBrewed = temporaryRecord[StatisticsType.potionsBrewed]!![player] as Int
                        this.itemsCrafted = temporaryRecord[StatisticsType.itemsCrafted]!![player] as Int
                        this.itemsEnchanted = temporaryRecord[StatisticsType.itemsEnchanted]!![player] as Int
                        this.toolsRepaired = temporaryRecord[StatisticsType.toolsRepaired]!![player] as Int
                        this.arrowShooted = temporaryRecord[StatisticsType.arrowShooted]!![player] as Int
                        this.arrowHitted = temporaryRecord[StatisticsType.arrowHitted]!![player] as Int
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }
    }

    fun onGameStart() {
        plugin.server.onlinePlayers.forEach { player ->
            locationCache[player] = player.location
        }
    }

    fun onGameEnd(
        missioned: Boolean
    ) {
        plugin.server.onlinePlayers.forEach { player ->
            temporaryRecord[StatisticsType.victoried]!![player] = if (player.isRunner()) missioned else !missioned
            temporaryRecord[StatisticsType.traveled]!![player] = player.location.distanceSquared(locationCache[player] ?: player.location)
            temporaryRecord[StatisticsType.openedLoots]!![player] = player.getStatistic(Statistic.CHEST_OPENED)
            temporaryRecord[StatisticsType.itemsTraded]!![player] = player.getStatistic(Statistic.TRADED_WITH_VILLAGER)
            temporaryRecord[StatisticsType.deathes]!![player] = player.getStatistic(Statistic.DEATHS)
            temporaryRecord[StatisticsType.itemsEnchanted]!![player] = player.getStatistic(Statistic.ITEM_ENCHANTED)
        }
    }

    @EventHandler
    fun onArrowHit(e: ProjectileCollideEvent) {
        val player = e.entity.shooter as Player
        temporaryRecord[StatisticsType.arrowHitted]!![player] = (temporaryRecord[StatisticsType.arrowHitted]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onArrowShoot(e: PlayerLaunchProjectileEvent) {
        val player = e.player
        val projectile = e.projectile
        if (projectile is Arrow) {
            temporaryRecord[StatisticsType.arrowShooted]!![player] = (temporaryRecord[StatisticsType.arrowShooted]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onAnvil(e: AnvilTakeResultEvent) {
        val player = e.player
        temporaryRecord[StatisticsType.toolsRepaired]!![player] = (temporaryRecord[StatisticsType.toolsRepaired]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onCraft(e: CraftItemEvent) {
        val player = e.whoClicked as Player
        val result = e.recipe.result
        val inv = e.inventory
        if (result.type.isEdible) {
            temporaryRecord[StatisticsType.foodsCrafted]!![player] = (temporaryRecord[StatisticsType.foodsCrafted]!![player] as? Int ?: 0).inc()
        }
        if (result.type == Material.IRON_INGOT) {
            temporaryRecord[StatisticsType.ironsSmelt]!![player] = (temporaryRecord[StatisticsType.ironsSmelt]!![player] as? Int ?: 0).inc()
        }
        if (result.type == Material.POTION || result.type == Material.SPLASH_POTION || result.type == Material.LINGERING_POTION) {
            temporaryRecord[StatisticsType.potionsBrewed]!![player] = (temporaryRecord[StatisticsType.potionsBrewed]!![player] as? Int ?: 0).inc()
        }
        if (inv.type == InventoryType.CRAFTING) {
            temporaryRecord[StatisticsType.itemsCrafted]!![player] = (temporaryRecord[StatisticsType.itemsCrafted]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onOpen(e: InventoryOpenEvent) {
        val player = e.player as Player
        val chest = player.world.getBlockAt(e.inventory.location ?: return).state as Chest
        if (chest.hasLootTable()) {
            temporaryRecord[StatisticsType.openedLoots]!![player] = (temporaryRecord[StatisticsType.openedLoots]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onBartar(e: EntityPickupItemEvent) {
        val player = Bukkit.getOfflinePlayer(e.item.owner ?: return)
        val entity = e.entity
        if (entity is Piglin) {
            temporaryRecord[StatisticsType.itemsBartered]!![player] = (temporaryRecord[StatisticsType.itemsBartered]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (locationCache[player] == null) locationCache[player] = player.location
    }

    @EventHandler
    fun onPlace(e: BlockPlaceEvent) {
        val player = e.player
        temporaryRecord[StatisticsType.blocksPlaced]!![player] = (temporaryRecord[StatisticsType.blocksPlaced]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        temporaryRecord[StatisticsType.blocksBroken]!![player] = (temporaryRecord[StatisticsType.blocksBroken]!![player] as? Int ?: 0).inc()
        if (block.type == Material.COAL_ORE || block.type == Material.DEEPSLATE_COAL_ORE) {
            temporaryRecord[StatisticsType.coalsMined]!![player] = (temporaryRecord[StatisticsType.coalsMined]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onEntityKilledEntity(e: EntityDeathEvent) {
        val entity = e.entity
        val player = e.entity.killer ?: return
        temporaryRecord[StatisticsType.mobsKilled]!![player] = (temporaryRecord[StatisticsType.mobsKilled]!![player] as? Int ?: 0).inc()
        when (entity) {
            is Enderman -> temporaryRecord[StatisticsType.endermansKilled]!![player] = (temporaryRecord[StatisticsType.endermansKilled]!![player] as? Int ?: 0).inc()
            is Blaze -> temporaryRecord[StatisticsType.blazesKilled]!![player] = (temporaryRecord[StatisticsType.blazesKilled]!![player] as? Int ?: 0).inc()
            is Player -> {
                if (entity in GameManager.runners()) {
                    temporaryRecord[StatisticsType.runnersKilled]!![player] = (temporaryRecord[StatisticsType.runnersKilled]!![player] as? Int ?: 0).inc()
                } else {
                    temporaryRecord[StatisticsType.huntersKilled]!![player] = (temporaryRecord[StatisticsType.huntersKilled]!![player] as? Int ?: 0).inc()
                }
            }
        }
    }

    @EventHandler
    fun onPortal(e: PlayerPortalEvent) {
        val enviroment = e.to.world.environment
        val player = e.player
        if (enviroment == World.Environment.THE_END) {
            temporaryRecord[StatisticsType.timeToTheEnd]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        } else if (enviroment == World.Environment.NETHER) {
            temporaryRecord[StatisticsType.timeToNether]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.player
        if (GameManager.started) {
            temporaryRecord[StatisticsType.aliveTime]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        }
    }

    fun clear() {
        StatisticsType.values().forEach { temporaryRecord[it] = mutableMapOf() }
        locationCache.clear()
    }
}
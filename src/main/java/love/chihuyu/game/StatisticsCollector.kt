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

    init {
        StatisticsType.values().forEach { temporaryRecord[it] = mutableMapOf() }
    }

    private val temporaryRecord = hashMapOf<StatisticsType, MutableMap<OfflinePlayer, Any>>()
    private val locationCache = mutableMapOf<OfflinePlayer, Location>()

    fun collect(
        startTime: LocalDateTime
    ) {
        plugin.server.onlinePlayers.forEach { player ->
            if (player.isHunter()) {
                Hunter.findOrNew(player.uniqueId, startTime) {
                    runCatching {
                        this.deathes = temporaryRecord[StatisticsType.DEATHES]!![player] as Int
                        this.runnersKilled = temporaryRecord[StatisticsType.RUNNERS_KILLED]!![player] as Int

                        this.victoried = temporaryRecord[StatisticsType.VICTORIED]!![player] as Boolean
                        this.aliveTime = temporaryRecord[StatisticsType.ALIVE_TIME]!![player] as Long
                        this.timeToTheEnd = temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] as Long
                        this.timeToNether = temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] as Long
                        this.mobsKilled = temporaryRecord[StatisticsType.MOBS_KILLED]!![player] as Int
                        this.endermansKilled = temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] as Int
                        this.blazesKilled = temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] as Int
                        this.blocksBroken = temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] as Int
                        this.blocksPlaced = temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] as Int
                        this.traveled = temporaryRecord[StatisticsType.TRAVELED]!![player] as Long
                        this.itemsTraded = temporaryRecord[StatisticsType.ITEMS_TRADED]!![player] as Int
                        this.itemsBartered = temporaryRecord[StatisticsType.ITEMS_BARTERED]!![player] as Int
                        this.openedLoots = temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] as Int
                        this.foodsCrafted = temporaryRecord[StatisticsType.FOODS_CRAFTED]!![player] as Int
                        this.ironsSmelt = temporaryRecord[StatisticsType.IRONS_SMELT]!![player] as Int
                        this.coalsMined = temporaryRecord[StatisticsType.COALS_MINED]!![player] as Int
                        this.potionsBrewed = temporaryRecord[StatisticsType.POTIONS_BREWED]!![player] as Int
                        this.itemsCrafted = temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] as Int
                        this.itemsEnchanted = temporaryRecord[StatisticsType.ITEMS_ENCHANTED]!![player] as Int
                        this.toolsRepaired = temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] as Int
                        this.arrowShooted = temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] as Int
                        this.arrowHitted = temporaryRecord[StatisticsType.ARROW_HITTED]!![player] as Int
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            } else if (player.isRunner()) {
                Runner.findOrNew(player.uniqueId, startTime) {
                    runCatching {
                        this.huntersKilled = temporaryRecord[StatisticsType.HUNTERS_KILLED]!![player] as Int

                        this.victoried = temporaryRecord[StatisticsType.VICTORIED]!![player] as Boolean
                        this.aliveTime = temporaryRecord[StatisticsType.ALIVE_TIME]!![player] as Long
                        this.timeToTheEnd = temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] as Long
                        this.timeToNether = temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] as Long
                        this.mobsKilled = temporaryRecord[StatisticsType.MOBS_KILLED]!![player] as Int
                        this.endermansKilled = temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] as Int
                        this.blazesKilled = temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] as Int
                        this.blocksBroken = temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] as Int
                        this.blocksPlaced = temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] as Int
                        this.traveled = temporaryRecord[StatisticsType.TRAVELED]!![player] as Long
                        this.itemsTraded = temporaryRecord[StatisticsType.ITEMS_TRADED]!![player] as Int
                        this.itemsBartered = temporaryRecord[StatisticsType.ITEMS_BARTERED]!![player] as Int
                        this.openedLoots = temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] as Int
                        this.foodsCrafted = temporaryRecord[StatisticsType.FOODS_CRAFTED]!![player] as Int
                        this.ironsSmelt = temporaryRecord[StatisticsType.IRONS_SMELT]!![player] as Int
                        this.coalsMined = temporaryRecord[StatisticsType.COALS_MINED]!![player] as Int
                        this.potionsBrewed = temporaryRecord[StatisticsType.POTIONS_BREWED]!![player] as Int
                        this.itemsCrafted = temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] as Int
                        this.itemsEnchanted = temporaryRecord[StatisticsType.ITEMS_ENCHANTED]!![player] as Int
                        this.toolsRepaired = temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] as Int
                        this.arrowShooted = temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] as Int
                        this.arrowHitted = temporaryRecord[StatisticsType.ARROW_HITTED]!![player] as Int
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
            temporaryRecord[StatisticsType.VICTORIED]!![player] = if (player.isRunner()) missioned else !missioned
            temporaryRecord[StatisticsType.TRAVELED]!![player] = player.location.distanceSquared(locationCache[player] ?: player.location)
            temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] = player.getStatistic(Statistic.CHEST_OPENED)
            temporaryRecord[StatisticsType.ITEMS_TRADED]!![player] = player.getStatistic(Statistic.TRADED_WITH_VILLAGER)
            temporaryRecord[StatisticsType.DEATHES]!![player] = player.getStatistic(Statistic.DEATHS)
            temporaryRecord[StatisticsType.ITEMS_ENCHANTED]!![player] = player.getStatistic(Statistic.ITEM_ENCHANTED)
        }
    }

    @EventHandler
    fun onArrowHit(e: ProjectileCollideEvent) {
        val player = e.entity.shooter as Player
        temporaryRecord[StatisticsType.ARROW_HITTED]!![player] = (temporaryRecord[StatisticsType.ARROW_HITTED]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onArrowShoot(e: PlayerLaunchProjectileEvent) {
        val player = e.player
        val projectile = e.projectile
        if (projectile is Arrow) {
            temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] = (temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onAnvil(e: AnvilTakeResultEvent) {
        val player = e.player
        temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] = (temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onCraft(e: CraftItemEvent) {
        val player = e.whoClicked as Player
        val result = e.recipe.result
        val inv = e.inventory
        if (result.type.isEdible) {
            temporaryRecord[StatisticsType.FOODS_CRAFTED]!![player] = (temporaryRecord[StatisticsType.FOODS_CRAFTED]!![player] as? Int ?: 0).inc()
        }
        if (result.type == Material.IRON_INGOT) {
            temporaryRecord[StatisticsType.IRONS_SMELT]!![player] = (temporaryRecord[StatisticsType.IRONS_SMELT]!![player] as? Int ?: 0).inc()
        }
        if (result.type == Material.POTION || result.type == Material.SPLASH_POTION || result.type == Material.LINGERING_POTION) {
            temporaryRecord[StatisticsType.POTIONS_BREWED]!![player] = (temporaryRecord[StatisticsType.POTIONS_BREWED]!![player] as? Int ?: 0).inc()
        }
        if (inv.type == InventoryType.CRAFTING) {
            temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] = (temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onOpen(e: InventoryOpenEvent) {
        val player = e.player as Player
        val chest = player.world.getBlockAt(e.inventory.location ?: return).state as Chest
        if (chest.hasLootTable()) {
            temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] = (temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onBartar(e: EntityPickupItemEvent) {
        val player = Bukkit.getOfflinePlayer(e.item.owner ?: return)
        val entity = e.entity
        if (entity is Piglin) {
            temporaryRecord[StatisticsType.ITEMS_BARTERED]!![player] = (temporaryRecord[StatisticsType.ITEMS_BARTERED]!![player] as? Int ?: 0).inc()
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
        temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] = (temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] = (temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] as? Int ?: 0).inc()
        if (block.type == Material.COAL_ORE || block.type == Material.DEEPSLATE_COAL_ORE) {
            temporaryRecord[StatisticsType.COALS_MINED]!![player] = (temporaryRecord[StatisticsType.COALS_MINED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onEntityKilledEntity(e: EntityDeathEvent) {
        val entity = e.entity
        val player = e.entity.killer ?: return
        temporaryRecord[StatisticsType.MOBS_KILLED]!![player] = (temporaryRecord[StatisticsType.MOBS_KILLED]!![player] as? Int ?: 0).inc()
        when (entity) {
            is Enderman -> temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] = (temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] as? Int ?: 0).inc()
            is Blaze -> temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] = (temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] as? Int ?: 0).inc()
            is Player -> {
                if (entity in GameManager.runners()) {
                    temporaryRecord[StatisticsType.RUNNERS_KILLED]!![player] = (temporaryRecord[StatisticsType.RUNNERS_KILLED]!![player] as? Int ?: 0).inc()
                } else {
                    temporaryRecord[StatisticsType.HUNTERS_KILLED]!![player] = (temporaryRecord[StatisticsType.HUNTERS_KILLED]!![player] as? Int ?: 0).inc()
                }
            }
        }
    }

    @EventHandler
    fun onPortal(e: PlayerPortalEvent) {
        val enviroment = e.to.world.environment
        val player = e.player
        if (enviroment == World.Environment.THE_END) {
            temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        } else if (enviroment == World.Environment.NETHER) {
            temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.player
        if (GameManager.started) {
            temporaryRecord[StatisticsType.ALIVE_TIME]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        }
    }

    fun clear() {
        StatisticsType.values().forEach { temporaryRecord[it] = mutableMapOf() }
        locationCache.clear()
    }
}

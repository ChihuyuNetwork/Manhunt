package love.chihuyu.game

import io.papermc.paper.event.player.AsyncChatEvent
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.database.User
import love.chihuyu.database.Users
import love.chihuyu.utils.TeamUtil.getTeam
import love.chihuyu.utils.TeamUtil.isRunner
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Blaze
import org.bukkit.entity.Enderman
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.purpurmc.purpur.event.inventory.AnvilTakeResultEvent
import java.time.Instant
import java.time.LocalDateTime

object StatisticsCollector : Listener {
    private val temporaryRecord = hashMapOf<StatisticsType, MutableMap<OfflinePlayer, Any>>()
    private val locationCache = mutableMapOf<OfflinePlayer, Location>()

    fun collect(
        startTime: LocalDateTime,
    ) {
        plugin.server.onlinePlayers.forEach { player ->
            runCatching {
                transaction {
                    addLogger(StdOutSqlLogger)
                    User.findOrNew(player.uniqueId, startTime) {
                        this.aliveTime = temporaryRecord[StatisticsType.ALIVE_TIME]!![player] as? Long ?: -1
                        this.arrowHitted = temporaryRecord[StatisticsType.ARROW_HITTED]!![player] as? Int ?: 0
                        this.arrowShooted = temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] as? Int ?: 0
                        this.blazesKilled = temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] as? Int ?: 0
                        this.blocksBroken = temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] as? Int ?: 0
                        this.blocksPlaced = temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] as? Int ?: 0
                        this.chats = temporaryRecord[StatisticsType.CHATS]!![player] as? Int ?: 0
                        this.coalsMined = temporaryRecord[StatisticsType.COALS_MINED]!![player] as? Int ?: 0
                        this.deathes = temporaryRecord[StatisticsType.DEATHES]!![player] as? Int ?: 0
                        this.endermansKilled = temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] as? Int ?: 0
                        this.gapplesUsed = temporaryRecord[StatisticsType.GAPPLES_USED]!![player] as? Int ?: 0
                        this.ironsSmelt = temporaryRecord[StatisticsType.IRONS_SMELT]!![player] as? Int ?: 0
                        this.itemsBartered = temporaryRecord[StatisticsType.ITEMS_BARTARED]!![player] as? Int ?: 0
                        this.itemsCrafted = temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] as? Int ?: 0
                        this.itemsEnchanted = temporaryRecord[StatisticsType.ITEMS_ENCHANTED]!![player] as? Int ?: 0
                        this.itemsTraded = temporaryRecord[StatisticsType.ITEMS_TRADED]!![player] as? Int ?: 0
                        this.mobsKilled = temporaryRecord[StatisticsType.MOBS_KILLED]!![player] as? Int ?: 0
                        this.openedLoots = temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] as? Int ?: 0
                        this.playersKilled = temporaryRecord[StatisticsType.PLAYERS_KILLED]!![player] as? Int ?: 0
                        this.potionsBrewed = temporaryRecord[StatisticsType.POTIONS_USED]!![player] as? Int ?: 0
                        this.team = temporaryRecord[StatisticsType.TEAM]!![player] as? Teams ?: Teams.HUNTER
                        this.timeToNether = temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] as? Long ?: -1
                        this.timeToTheEnd = temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] as? Long ?: -1
                        this.toolsRepaired = temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] as? Int ?: 0
                        this.traveled = temporaryRecord[StatisticsType.TRAVELED]!![player] as? Long ?: 0
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun onGameStart() {
        clear()
        plugin.server.onlinePlayers.forEach { player ->
            locationCache[player] = player.location
        }
    }

    fun onGameEnd() {
        plugin.server.onlinePlayers.forEach { player ->
            transaction {
                val statisticData = Users.select { (Users.uuid eq player.uniqueId) and (Users.team eq player.getTeam()) }
                if (temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] == null)
                    temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] =
                        try {
                            statisticData.sumOf { it[Users.timeToTheEnd] } / Users.select { (Users.uuid eq player.uniqueId) and (Users.team eq player.getTeam()) and (Users.timeToTheEnd neq -1) }.count()
                        } catch (e: ArithmeticException) {
                            0
                        }
                if (temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] == null)
                    temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] =
                        try {
                            statisticData.sumOf { it[Users.timeToNether] } / Users.select { (Users.uuid eq player.uniqueId) and (Users.team eq player.getTeam()) and (Users.timeToNether neq -1) }.count()
                        } catch (e: ArithmeticException) {
                            0
                        }
            }
            if (temporaryRecord[StatisticsType.ALIVE_TIME]!![player] == null) temporaryRecord[StatisticsType.ALIVE_TIME]!![player] = Instant.now().epochSecond - GameManager.startEpoch
            temporaryRecord[StatisticsType.DEATHES]!![player] = player.getStatistic(Statistic.DEATHS)
            temporaryRecord[StatisticsType.ITEMS_ENCHANTED]!![player] = player.getStatistic(Statistic.ITEM_ENCHANTED)
            temporaryRecord[StatisticsType.ITEMS_TRADED]!![player] = player.getStatistic(Statistic.TRADED_WITH_VILLAGER)
            temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] = player.getStatistic(Statistic.CHEST_OPENED)
            temporaryRecord[StatisticsType.PLAYERS_KILLED]!![player] = player.getStatistic(Statistic.PLAYER_KILLS)
            temporaryRecord[StatisticsType.TEAM]!![player] = if (player.isRunner()) Teams.RUNNER else Teams.HUNTER
            temporaryRecord[StatisticsType.TRAVELED]!![player] = player.location.distance(locationCache[player] ?: player.location).toLong()
        }
    }

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        val player = e.player
        temporaryRecord[StatisticsType.CHATS]!![player] =
            (temporaryRecord[StatisticsType.CHATS]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onArrowHit(e: ProjectileHitEvent) {
        val player = e.entity.shooter as? Player ?: return
        if (e.hitEntity == null) return
        temporaryRecord[StatisticsType.ARROW_HITTED]!![player] =
            (temporaryRecord[StatisticsType.ARROW_HITTED]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onArrowShoot(e: EntityShootBowEvent) {
        val player = e.entity as? Player ?: return
        val projectile = e.projectile.type
        if (projectile == EntityType.ARROW) {
            temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] =
                (temporaryRecord[StatisticsType.ARROW_SHOOTED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onAnvil(e: AnvilTakeResultEvent) {
        val player = e.player
        temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] =
            (temporaryRecord[StatisticsType.TOOLS_REPAIRED]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onCraft(e: CraftItemEvent) {
        val player = e.whoClicked as Player
        val inv = e.inventory
        if (inv.type == InventoryType.CRAFTING || inv.type == InventoryType.PLAYER) {
            temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] =
                (temporaryRecord[StatisticsType.ITEMS_CRAFTED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onSmelt(e: FurnaceExtractEvent) {
        val player = e.player
        val result = e.itemType
        if (result == Material.IRON_INGOT) {
            temporaryRecord[StatisticsType.IRONS_SMELT]!![player] =
                (temporaryRecord[StatisticsType.IRONS_SMELT]!![player] as? Int ?: 0) + e.itemAmount
        }
    }

    @EventHandler
    fun onConsume(e: PlayerItemConsumeEvent) {
        val player = e.player
        val item = e.item
        if (item.type == Material.POTION || item.type == Material.SPLASH_POTION || item.type == Material.LINGERING_POTION) {
            temporaryRecord[StatisticsType.POTIONS_USED]!![player] =
                (temporaryRecord[StatisticsType.POTIONS_USED]!![player] as? Int ?: 0).inc()
        }
        if (item.type == Material.GOLDEN_APPLE || item.type == Material.ENCHANTED_GOLDEN_APPLE) {
            temporaryRecord[StatisticsType.GAPPLES_USED]!![player] =
                (temporaryRecord[StatisticsType.GAPPLES_USED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onOpen(e: InventoryOpenEvent) {
        val player = e.player as Player
        val chest = player.world.getBlockAt(e.inventory.location ?: return).state as? Chest ?: return
        if (chest.hasLootTable()) {
            temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] =
                (temporaryRecord[StatisticsType.OPENED_LOOTS]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onBartar(e: EntityPickupItemEvent) {
        val player = Bukkit.getOfflinePlayer(e.item.thrower ?: return)
        val entity = e.entityType
        if (entity == EntityType.PIGLIN) {
            temporaryRecord[StatisticsType.ITEMS_BARTARED]!![player] =
                (temporaryRecord[StatisticsType.ITEMS_BARTARED]!![player] as? Int ?: 0).inc()
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
        temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] =
            (temporaryRecord[StatisticsType.BLOCKS_PLACED]!![player] as? Int ?: 0).inc()
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] =
            (temporaryRecord[StatisticsType.BLOCKS_BROKEN]!![player] as? Int ?: 0).inc()
        if (block.type == Material.COAL_ORE || block.type == Material.DEEPSLATE_COAL_ORE) {
            temporaryRecord[StatisticsType.COALS_MINED]!![player] =
                (temporaryRecord[StatisticsType.COALS_MINED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onEntityKilledEntity(e: EntityDeathEvent) {
        val entity = e.entity
        val player = e.entity.killer ?: return
        temporaryRecord[StatisticsType.MOBS_KILLED]!![player] =
            (temporaryRecord[StatisticsType.MOBS_KILLED]!![player] as? Int ?: 0).inc()
        when (entity) {
            is Enderman -> temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] =
                (temporaryRecord[StatisticsType.ENDERMANS_KILLED]!![player] as? Int ?: 0).inc()

            is Blaze -> temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] =
                (temporaryRecord[StatisticsType.BLAZES_KILLED]!![player] as? Int ?: 0).inc()
        }
    }

    @EventHandler
    fun onPortal(e: PlayerPortalEvent) {
        val enviroment = e.to.world.environment
        val player = e.player
        if (enviroment == World.Environment.THE_END) {
            temporaryRecord[StatisticsType.TIME_TO_THE_END]!![player] =
                Instant.now().epochSecond - GameManager.startEpoch
        } else if (enviroment == World.Environment.NETHER) {
            temporaryRecord[StatisticsType.TIME_TO_NETHER]!![player] =
                Instant.now().epochSecond - GameManager.startEpoch
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.player
        if (player.isRunner()) {
            temporaryRecord[StatisticsType.ALIVE_TIME]!![player] = Instant.now().epochSecond - GameManager.startEpoch
        }
    }

    fun clear() {
        StatisticsType.values().forEach { temporaryRecord[it] = mutableMapOf() }
        locationCache.clear()
    }
}

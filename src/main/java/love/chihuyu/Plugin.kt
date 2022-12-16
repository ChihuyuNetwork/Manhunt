package love.chihuyu

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import love.chihuyu.commands.CommandManhunt
import love.chihuyu.game.EventCanceller
import love.chihuyu.game.GameManager
import love.chihuyu.game.GameManager.hunterTeamName
import love.chihuyu.game.GameManager.hunters
import love.chihuyu.game.GameManager.runners
import love.chihuyu.game.MissionChecker
import love.chihuyu.utils.CompassUtil
import love.chihuyu.utils.ItemUtil
import love.chihuyu.utils.runTaskLater
import love.chihuyu.utils.runTaskTimer
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var compassTask: BukkitTask
        lateinit var protocolManager: ProtocolManager
        var cooltimed = mutableSetOf<Player>()
        val prefix = "${ChatColor.GOLD}[MH]${ChatColor.RESET}"
        val compassTargets = mutableMapOf<Player, Player>()
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager()

        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(MissionChecker, this)
        server.pluginManager.registerEvents(EventCanceller, this)

        compassTask = runTaskTimer(0, 0) {
            server.onlinePlayers.forEach {
                val target = compassTargets[it]
                it.sendActionBar(Component.text("${ChatColor.WHITE}追跡中 ≫ " + target?.name))
            }
        }

        CommandManhunt.register()
    }

    override fun onDisable() {
        compassTask.cancel()
    }

    @EventHandler
    fun onMine(e: BlockBreakEvent) {
        if (e.block.type in listOf(
                Material.COAL_ORE,
                Material.COPPER_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.GOLD_ORE,
                Material.IRON_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.DEEPSLATE_COAL_ORE,
                Material.DEEPSLATE_COPPER_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.DEEPSLATE_LAPIS_ORE,
                Material.DEEPSLATE_REDSTONE_ORE,
                Material.NETHER_GOLD_ORE,
                Material.NETHER_QUARTZ_ORE,
        ))
            e.expToDrop += 24
    }

    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        val player = e.player

        plugin.runTaskLater(1) {
            ItemUtil.giveCompassIfNone(player)
            player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.entity

        e.drops.removeIf { it.type == Material.COMPASS }
        if (player in runners()) player.gameMode = GameMode.SPECTATOR

        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        if (player !in hunters() && player !in runners()) {
            GameManager.board.getTeam(hunterTeamName)?.addPlayer(player)
        }

        val obj = GameManager.board.getObjective("health") ?: GameManager.board.registerNewObjective("health", Criteria.HEALTH, Component.text("${ChatColor.RED}♥"))
        obj.displaySlot = DisplaySlot.BELOW_NAME

        player.scoreboard = GameManager.board

        player.gameMode =
            if (player.gameMode == GameMode.SPECTATOR) {
                GameMode.SPECTATOR
            } else {
                GameMode.SURVIVAL
            }

        ItemUtil.giveCompassIfNone(player)
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))
    }

    @EventHandler
    fun onHit(e: ProjectileHitEvent) {
        if (e.hitEntity is Player) (e.hitEntity as Player).damage(0.1)
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = e.player

        if (player.gameMode == GameMode.SPECTATOR) {
            e.format = "${ChatColor.GRAY}[SPEC]${ChatColor.RESET} ${player.name}: ${e.message}"
            return
        }

        val isGlobal = e.message.startsWith('!')
        val teamColor = if (isGlobal) ChatColor.DARK_PURPLE else GameManager.board.getPlayerTeam(player)?.color ?: ChatColor.AQUA
        val teamPrefix =
            when (player) {
                in hunters() -> "$teamColor[H]${ChatColor.RESET}"
                in runners() -> "$teamColor[R]${ChatColor.RESET}"
                else -> "$teamColor[N]${ChatColor.RESET}"
            }

        e.recipients.removeIf { !isGlobal && !(GameManager.board.getPlayerTeam(player)?.hasPlayer(it) ?: true) }

        if (isGlobal) e.message = e.message.substringAfter('!')
        e.format = "$teamPrefix ${player.name}: ${e.message}"
    }

    @EventHandler
    fun logToPlanks(e: PlayerInteractEvent) {
        val player = e.player
        val action = e.action
        val item = e.item ?: return
        if (action != Action.RIGHT_CLICK_AIR) return
        if (item.type !in listOf(
                Material.OAK_LOG,
                Material.DARK_OAK_LOG,
                Material.JUNGLE_LOG,
                Material.ACACIA_LOG,
                Material.MANGROVE_LOG,
                Material.BIRCH_LOG,
                Material.SPRUCE_LOG,
            )) return

        val amount = player.inventory.itemInMainHand.amount
        player.inventory.itemInMainHand.amount = 0
        player.inventory.addItem(ItemUtil.create(Material.OAK_PLANKS, amount = amount * 4)).forEach { (amount, itemStack) ->
            player.world.dropItemNaturally(player.location, ItemUtil.create(itemStack.type, amount = amount))
        }
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
    }

    @EventHandler
    fun compassTracker(e: PlayerInteractEvent) {
        val player = e.player
        val action = e.action
        val item = e.item ?: return
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return
        if (item.type != Material.COMPASS) return

        val nextPlayer = try {
            val other = plugin.server.onlinePlayers.toList().minus(player)
            other[other.indexOf(compassTargets[player]).inc() % other.size]
        } catch (e: Exception) {
            return
        }
        compassTargets[player] = nextPlayer

        CompassUtil.setTargetTo(player, nextPlayer)
    }

    @EventHandler
    fun updateCompassTracking(e: PlayerMoveEvent) {
        val player = e.player
        if (player in cooltimed) return
        compassTargets.filter { it.value == player }.forEach { (hunter, target) ->
            CompassUtil.setTargetTo(hunter, target)
        }

        cooltimed.add(player)

        plugin.runTaskLater(20) { cooltimed.remove(player) }
    }
}
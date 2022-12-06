package love.chihuyu

import love.chihuyu.commands.CommandManhunt
import love.chihuyu.game.GameManager
import love.chihuyu.game.GameManager.hunterTeamName
import love.chihuyu.game.GameManager.hunters
import love.chihuyu.game.GameManager.runners
import love.chihuyu.game.GameManager.started
import love.chihuyu.game.MissionChecker
import love.chihuyu.utils.CompassUtil
import love.chihuyu.utils.ItemUtil
import love.chihuyu.utils.runTaskLater
import love.chihuyu.utils.runTaskTimer
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var compassTask: BukkitTask
        var cooltimed = mutableSetOf<Player>()
        val prefix = "${ChatColor.GOLD}[MH]${ChatColor.RESET}"
        val compassTargets = mutableMapOf<Player, Player>()
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(MissionChecker, this)

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
    fun onRespawn(e: PlayerRespawnEvent) {
        val player = e.player
        ItemUtil.giveCompassIfNone(player)
        if (player.gameMode == GameMode.SPECTATOR) player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        e.drops.removeIf { it.itemMeta.hasCustomModelData() }
        if (e.entity in runners()) e.entity.gameMode = GameMode.SPECTATOR
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        e.isCancelled = !started
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        if (player !in hunters() || player !in runners()) {
            GameManager.board.getTeam(hunterTeamName)?.addPlayer(player)
        }

        player.gameMode = if (started) {
            if (player.gameMode == GameMode.SPECTATOR) {
                GameMode.SPECTATOR
            } else {
                GameMode.SURVIVAL
            }
        } else {
            GameMode.ADVENTURE
        }

        ItemUtil.giveCompassIfNone(player)
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = e.player

        if (player.gameMode == GameMode.SPECTATOR) {
            e.recipients.removeIf { it.gameMode != GameMode.SPECTATOR }
            e.format = "${ChatColor.GRAY}[SPEC]${ChatColor.RESET} ${player.name}: ${e.message}"
            return
        }

        val isTeam = !e.message.startsWith('!')
        val teamColor = if (isTeam) ChatColor.DARK_PURPLE else GameManager.board.getPlayerTeam(player)?.color ?: ChatColor.AQUA
        val teamPrefix =
            when (player) {
                in hunters() -> "$teamColor[H]${ChatColor.RESET}"
                in runners() -> "$teamColor[E]${ChatColor.RESET}"
                else -> "$teamColor[N]${ChatColor.RESET}"
            }

        e.recipients.removeIf { !isTeam && !(GameManager.board.getPlayerTeam(player)?.hasPlayer(it) ?: true) }

        if (!isTeam) {
            e.message = e.message.substringAfter('!')
        }
        e.format = "$teamPrefix ${player.name}: ${e.message}"
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        e.isCancelled = !started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onEntityInteract(e: PlayerInteractEntityEvent) {
        e.isCancelled = !started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onBlockDamage(e: BlockDamageEvent) {
        e.isCancelled = !started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onPick(e: EntityPickupItemEvent) {
        e.isCancelled = !started && (e.entity as? Player ?: return).gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onHunger(e: FoodLevelChangeEvent) {
        e.isCancelled = !started
    }

    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        e.isCancelled = !started && e.player.gameMode != GameMode.CREATIVE
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
        } catch (e: NoSuchElementException) {
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
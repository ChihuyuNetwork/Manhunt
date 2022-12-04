package love.chihuyu

import love.chihuyu.commands.CommandManhunt
import love.chihuyu.game.GameManager
import love.chihuyu.game.GameManager.escapers
import love.chihuyu.game.GameManager.hunters
import love.chihuyu.game.GameManager.started
import love.chihuyu.game.MissionChecker
import love.chihuyu.utils.ItemUtil
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        val prefix = "${ChatColor.GOLD}[MH]${ChatColor.RESET}"
        val compassTargets = mutableMapOf<Player, Player>()
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(MissionChecker, this)

        CommandManhunt.main.register()
    }

    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        ItemUtil.giveCompassIfNone(e.player)
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        e.drops.removeIf { it.itemMeta.hasCustomModelData() }
        if (e.player in escapers()) e.player.gameMode = GameMode.SPECTATOR
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        e.isCancelled = !started && e.entity is Player
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
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
        val teamColor = GameManager.board.getPlayerTeam(player)?.color ?: ChatColor.WHITE
        var teamPrefix =
            when (player) {
                in hunters() -> "$teamColor[H]${ChatColor.RESET}"
                in escapers() -> "$teamColor[E]${ChatColor.RESET}"
                else -> "$teamColor[N]${ChatColor.RESET}"
            }
        val isAll = e.message.startsWith('!')

        e.recipients.removeIf { !isAll && !(GameManager.board.getPlayerTeam(player)?.hasPlayer(it) ?: true) }

        if (isAll) {
            teamPrefix = "$teamColor[A]${ChatColor.RESET}"
            e.message = e.message.substringAfter('!')
        }
        e.format = "$teamPrefix ${player.name}: ${e.message}"
    }

    @EventHandler
    fun compassTracker(e: PlayerInteractEvent) {
        val player = e.player
        val action = e.action
        val item = e.item ?: return
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return
        if (item.type != Material.COMPASS) return

        val nextPlayer = plugin.server.onlinePlayers.toList()[plugin.server.onlinePlayers.indexOf(compassTargets[player]).inc() % plugin.server.onlinePlayers.toList().size]
        player.compassTarget = nextPlayer.location
        compassTargets[player] = nextPlayer

        val meta = item.itemMeta as CompassMeta
        meta.isLodestoneTracked = false
        meta.lodestone = nextPlayer.location
        meta.displayName(Component.text(nextPlayer.name))
        player.inventory.itemInMainHand.itemMeta = meta
    }
}
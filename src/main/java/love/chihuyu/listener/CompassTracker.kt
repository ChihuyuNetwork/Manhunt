package love.chihuyu.listener

import love.chihuyu.Plugin
import love.chihuyu.utils.CompassUtil
import love.chihuyu.utils.runTaskLater
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

object CompassTracker : Listener {

    @EventHandler
    fun compassTracker(e: PlayerInteractEvent) {
        val player = e.player
        val action = e.action
        val item = e.item ?: return
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return
        if (item.type != Material.COMPASS) return

        val nextPlayer = try {
            val other = Plugin.plugin.server.onlinePlayers.toList().minus(player).filter { it.gameMode == GameMode.SURVIVAL }
            other[(other.indexOf(Plugin.compassTargets[player]) + (if (player.isSneaking) -1 else 1)) % other.size]
        } catch (e: Exception) {
            return
        }
        Plugin.compassTargets[player] = nextPlayer

        CompassUtil.setTargetTo(player, nextPlayer)
    }

    @EventHandler
    fun updateCompassTracking(e: PlayerMoveEvent) {
        val player = e.player
        if (player in Plugin.cooltimed) return
        Plugin.compassTargets.filter { it.value == player }.forEach { (hunter, target) ->
            CompassUtil.setTargetTo(hunter, target)
        }

        Plugin.cooltimed.add(player)

        Plugin.plugin.runTaskLater(20) { Plugin.cooltimed.remove(player) }
    }
}

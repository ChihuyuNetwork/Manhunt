package love.chihuyu.game

import love.chihuyu.game.GameManager.mission
import love.chihuyu.game.GameManager.started
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerPortalEvent

object MissionChecker : Listener {

    @EventHandler
    fun aliveEscapers(e: PlayerDeathEvent) {
        if (GameManager.escapers().none { started && it.gameMode != GameMode.SPECTATOR }) GameManager.end(false)
    }

    @EventHandler
    fun checkPortalMission(e: PlayerPortalEvent) {
        if (e.to.world.environment == World.Environment.THE_END && mission == ManhuntMission.ENTER_END_PORTAL) GameManager.end(true)
    }

    @EventHandler
    fun checkEntityKillMission(e: EntityDeathEvent) {
        if (e.entity.type == EntityType.ENDER_DRAGON && mission == ManhuntMission.KILL_ENDER_DRAGON) GameManager.end(true)
    }
}
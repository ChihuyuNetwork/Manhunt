package love.chihuyu.game

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object EventCanceller : Listener {

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        e.isCancelled =
            !GameManager.started &&
            if (e is EntityDamageByEntityEvent)
                if (e.damager is Player)
                    (e.damager as Player).gameMode != GameMode.CREATIVE
                else
                    true
            else
                e.cause != EntityDamageEvent.DamageCause.VOID
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        e.isCancelled = !GameManager.started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onEntityInteract(e: PlayerInteractEntityEvent) {
        e.isCancelled = !GameManager.started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onBlockDamage(e: BlockDamageEvent) {
        e.isCancelled = !GameManager.started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onPick(e: EntityPickupItemEvent) {
        e.isCancelled = !GameManager.started && (e.entity as? Player ?: return).gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onHunger(e: FoodLevelChangeEvent) {
        e.isCancelled = !GameManager.started
    }

    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        e.isCancelled = !GameManager.started && e.player.gameMode != GameMode.CREATIVE
    }

    @EventHandler
    fun onAchievement(e: PlayerAdvancementCriterionGrantEvent) {
        e.isCancelled = e.player.gameMode == GameMode.SPECTATOR
    }
}
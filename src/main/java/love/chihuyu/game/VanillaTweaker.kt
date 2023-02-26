package love.chihuyu.game

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object VanillaTweaker: Listener {

    @EventHandler
    fun onRespawn(e: PlayerPostRespawnEvent) {
        val player = e.player
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false, false))
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.entity

        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false, false))
    }
}
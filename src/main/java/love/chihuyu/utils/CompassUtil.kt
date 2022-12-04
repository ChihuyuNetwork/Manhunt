package love.chihuyu.utils

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.CompassMeta

object CompassUtil {

    fun setTargetTo(player: Player, target: Player) {
        player.compassTarget = target.location

        player.inventory.filterNotNull().filter { it.type == Material.COMPASS}.forEach { item ->
            val meta = item.itemMeta as CompassMeta
            meta.isLodestoneTracked = false
            meta.lodestone = target.location
            item.itemMeta = meta
        }
    }
}
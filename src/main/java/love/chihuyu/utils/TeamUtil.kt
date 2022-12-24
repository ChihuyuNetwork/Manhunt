package love.chihuyu.utils

import love.chihuyu.game.GameManager
import org.bukkit.entity.Player

object TeamUtil {

    fun Player.isRunner(): Boolean {
        return this in GameManager.runners()
    }

    fun Player.isHunter(): Boolean {
        return this in GameManager.hunters()
    }
}
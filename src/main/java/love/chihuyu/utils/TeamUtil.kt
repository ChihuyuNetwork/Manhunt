package love.chihuyu.utils

import love.chihuyu.game.GameManager
import love.chihuyu.game.Teams
import org.bukkit.entity.Player

object TeamUtil {

    fun Player.isRunner(): Boolean {
        return getTeam() == Teams.RUNNER
    }

    fun Player.isHunter(): Boolean {
        return getTeam() == Teams.HUNTER
    }

    fun Player.getTeam(): Teams {
        return when (GameManager.board.getPlayerTeam(this)?.name) {
            "runner" -> Teams.RUNNER
            "hunter" -> Teams.HUNTER
            else -> Teams.HUNTER
        }
    }
}

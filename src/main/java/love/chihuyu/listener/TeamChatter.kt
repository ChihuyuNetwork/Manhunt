package love.chihuyu.listener

import love.chihuyu.game.GameManager
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

object TeamChatter: Listener {

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = e.player
        val isGlobal = e.message.startsWith('!') || if (player.hasMetadata("mh_globalchat")) {
            player.getMetadata("mh_globalchat")[0].asBoolean()
        } else false

        if (!e.isAsynchronous) return

        if (player.gameMode == GameMode.SPECTATOR) {
            if (isGlobal) {
                e.message = e.message.substringAfter('!')
            } else {
                e.recipients.removeIf { it.gameMode != GameMode.SPECTATOR }
            }
            e.format =
                "${if (isGlobal) ChatColor.DARK_PURPLE else ChatColor.GRAY}[SPEC]${ChatColor.RESET} ${player.name}: ${e.message}"
            return
        }

        val teamColor =
            if (isGlobal) ChatColor.DARK_PURPLE else GameManager.board.getPlayerTeam(player)?.color ?: ChatColor.AQUA
        val teamPrefix =
            when (player) {
                in GameManager.hunters() -> "$teamColor[HUNT]${ChatColor.RESET}"
                in GameManager.runners() -> "$teamColor[RUN]${ChatColor.RESET}"
                else -> "$teamColor[NULL]${ChatColor.RESET}"
            }

        e.recipients.removeIf { !isGlobal && !(GameManager.board.getPlayerTeam(player)?.hasPlayer(it) ?: true) }

        if (isGlobal) e.message = e.message.substringAfter('!')
        e.format = "$teamPrefix ${player.name}: ${e.message}"
    }
}
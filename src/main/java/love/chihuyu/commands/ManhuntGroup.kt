package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.game.GameManager
import love.chihuyu.game.GameManager.escaperTeamName
import love.chihuyu.game.GameManager.escapers
import love.chihuyu.game.GameManager.hunterTeamName
import love.chihuyu.game.GameManager.hunters
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.scoreboard.Team

object ManhuntGroup {

    val main = CommandAPICommand("group")
        .withArguments(IntegerArgument("escapers", 1))
        .executes(
            CommandExecutor { sender, args ->
                GameManager.board.teams.forEach(Team::unregister)

                val hunterTeam = GameManager.board.registerNewTeam(hunterTeamName)
                val escaperTeam = GameManager.board.registerNewTeam(escaperTeamName)
                val escapersCount = args[0] as Int

                hunterTeam.color(NamedTextColor.WHITE)
                escaperTeam.color(NamedTextColor.RED)

                hunters().forEach { hunterTeam.removePlayer(it) }
                escapers().forEach { escaperTeam.removePlayer(it) }

                (0..escapersCount).forEach { _ ->
                    escaperTeam.addPlayer(plugin.server.onlinePlayers.minus(escapers()).random())
                }

                plugin.server.onlinePlayers.minus(escapers()).forEach {
                    hunterTeam.addPlayer(it)
                }

                sender.sendMessage("$prefix Successfully grouped escapers and hunters.")
            }
        )
}
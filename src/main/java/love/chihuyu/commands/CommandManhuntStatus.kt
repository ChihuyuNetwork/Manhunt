package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.database.Matches
import love.chihuyu.gui.StatisticsScreen
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object CommandManhuntStatus {

    val main: CommandAPICommand = CommandAPICommand("manhuntstatus")
        .withAliases("mhstats")
        .withPermission(CommandPermission.NONE)
        .withArguments(OfflinePlayerArgument("player"))
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            StatisticsScreen.openStatistics(sender, args[0] as OfflinePlayer)
        })

    val specifiedDate: CommandAPICommand = CommandAPICommand("manhuntstatus")
        .withAliases("mhstats")
        .withPermission(CommandPermission.NONE)
        .withArguments(OfflinePlayerArgument("player"), StringArgument("date").replaceSuggestions(ArgumentSuggestions.strings(
            transaction {
                Matches.selectAll().map { it[Matches.date].toString() }
            }
        )))
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            StatisticsScreen.openStatistics(sender, args[0] as OfflinePlayer, LocalDateTime.parse(args[1] as String))
        })
}
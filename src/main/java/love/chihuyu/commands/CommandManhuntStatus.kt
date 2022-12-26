package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.database.Users
import love.chihuyu.game.Teams
import love.chihuyu.gui.StatisticsScreen
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object CommandManhuntStatus {

    val main: CommandAPICommand = CommandAPICommand("manhuntstatus")
        .withAliases("mhstats")
        .withPermission(CommandPermission.NONE)
        .withArguments(OfflinePlayerArgument("player").replaceSuggestions(ArgumentSuggestions.strings {
            transaction {
                Users.selectAll().map { Bukkit.getOfflinePlayer(it[Users.uuid]).name }.toTypedArray()
            }
        }))
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            StatisticsScreen.openStatistics(sender, args[0] as OfflinePlayer, Teams.HUNTER)
        })

    val specifiedDate: CommandAPICommand = CommandAPICommand("manhuntstatus")
        .withAliases("mhstats")
        .withPermission(CommandPermission.NONE)
        .withArguments(OfflinePlayerArgument("player").replaceSuggestions(ArgumentSuggestions.strings {
            transaction {
                Users.selectAll().map { Bukkit.getOfflinePlayer(it[Users.uuid]).name }.toTypedArray()
            }
        }), GreedyStringArgument("date").replaceSuggestions(ArgumentSuggestions.strings(
            transaction {
                Users.selectAll().map { "${it[Users.date]}" }
            }
        )))
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            StatisticsScreen.openStatistics(sender, args[0] as OfflinePlayer, Teams.HUNTER, LocalDateTime.parse(args[1] as String))
        })
}
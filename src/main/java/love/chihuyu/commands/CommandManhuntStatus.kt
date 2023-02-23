package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.database.NameRecord
import love.chihuyu.database.Users
import love.chihuyu.game.Teams
import love.chihuyu.gui.StatisticsScreen
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

object CommandManhuntStatus {

    private fun getPlayerName(uuid: UUID): String = transaction {
        (NameRecord.select { NameRecord.uuid eq uuid }.singleOrNull() ?: return@transaction "")[NameRecord.ign]
    }
    private fun playerArgSuggest() = CompletableFuture.supplyAsync {
        transaction { Users.selectAll().map { getPlayerName(it[Users.uuid]) }.toSet().toTypedArray() }
    }.get()

    val main: CommandAPICommand = CommandAPICommand("manhuntstatus")
        .withAliases("mhstats")
        .withPermission(CommandPermission.OP)
        .withArguments(
            OfflinePlayerArgument("player").replaceSuggestions(
                ArgumentSuggestions.strings { playerArgSuggest() }
            )
        )
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                StatisticsScreen.openStatistics(sender, args[0] as OfflinePlayer, Teams.HUNTER)
            }
        )

    val specifiedDate: CommandAPICommand = CommandAPICommand("manhuntstatus")
        .withAliases("mhstats")
        .withPermission(CommandPermission.OP)
        .withArguments(
            OfflinePlayerArgument("player").replaceSuggestions(
                ArgumentSuggestions.strings { playerArgSuggest() }
            ),
            GreedyStringArgument("date").replaceSuggestions(
                ArgumentSuggestions.strings { info ->
                    CompletableFuture.supplyAsync {
                        transaction { Users.select { Users.uuid eq (info.previousArgs[0] as OfflinePlayer).uniqueId }.map { "${it[Users.date]}" }.toTypedArray() }
                    }.get()
                }
            )
        )
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                transaction {
                    val date = LocalDateTime.parse(args[1] as String)
                    val selector = Users.select { (Users.uuid eq (args[0] as OfflinePlayer).uniqueId) and (Users.date eq date) }.single()
                    StatisticsScreen.openStatistics(sender, args[0] as OfflinePlayer, selector[Users.team], date)
                }
            }
        )
}

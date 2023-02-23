package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.database.Matches
import org.bukkit.ChatColor
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

object CommandManhuntMatch {

    val main = CommandAPICommand("manhuntmatch")
        .withAliases("mhmatch")
        .withPermission(CommandPermission.OP)
        .withArguments(
            GreedyStringArgument("date").replaceSuggestions(
                ArgumentSuggestions.stringsAsync {
                    CompletableFuture.supplyAsync {
                        transaction { Matches.selectAll().map { "${it[Matches.date]}" }.toTypedArray() }
                    }
                }
            )
        )
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                transaction {
                    val date = LocalDateTime.parse(args[0] as String)
                    val matchData = Matches.select { Matches.date eq date }.single()

                    sender.sendMessage(
                        """
                ${ChatColor.GOLD}======${ChatColor.WHITE}${date.year}/${"%02d".format(date.month.value)}/${"%02d".format(date.dayOfMonth)} ${"%02d".format(date.hour)}:${"%02d".format(date.minute)}:${"%02d".format(date.second)}の情報${ChatColor.GOLD}======${ChatColor.WHITE}
                試合時間: ${matchData[Matches.matchTime]}秒
                勝利チーム: ${matchData[Matches.winnerTeam]}
                シード: ${matchData[Matches.seed]}
                ${ChatColor.GOLD}==================================
                        """.trimIndent()
                    )
                }
            }
        )
}

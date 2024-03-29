package love.chihuyu

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import love.chihuyu.database.Matches
import love.chihuyu.database.NameRecord
import love.chihuyu.database.Users
import love.chihuyu.game.*
import love.chihuyu.gui.StatisticsScreen
import love.chihuyu.listener.CompassTracker
import love.chihuyu.listener.GameListener
import love.chihuyu.listener.TeamChatter
import love.chihuyu.utils.runTaskTimer
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var compassTask: BukkitTask
        var cooltimed = mutableSetOf<Player>()
        val prefix = "${ChatColor.GOLD}[MH]${ChatColor.RESET}"
        val compassTargets = mutableMapOf<Player, Player>()
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        StatisticsCollector.clear()
        initCommands()

        listOf(
            MissionChecker,
            EventCanceller,
            StatisticsCollector,
            StatisticsScreen,
            VanillaTweaker,
            CompassTracker,
            GameListener,
            TeamChatter
        ).forEach { server.pluginManager.registerEvents(it, this) }

        compassTask = runTaskTimer(0, 0) {
            server.onlinePlayers.forEach {
                val target = compassTargets[it]
                it.sendActionBar(Component.text("${ChatColor.WHITE}追跡中 ≫ " + target?.name))
            }
        }

        val dbFile = File("${plugin.dataFolder}/statistics.db")
        if (!dbFile.exists()) {
            File("${plugin.dataFolder}").mkdir()
            dbFile.createNewFile()
        }

        Database.connect("jdbc:sqlite:${plugin.dataFolder}/statistics.db", "org.sqlite.JDBC")
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Users, withLogs = true)
            SchemaUtils.createMissingTablesAndColumns(Matches, withLogs = true)
            SchemaUtils.createMissingTablesAndColumns(NameRecord, withLogs = true)
        }

        RecipeManager.add()
    }

    override fun onDisable() {
        compassTask.cancel()
    }

    private fun initCommands() {
        commandAPICommand("manhunt", { sender -> sender.isOp }) {
            subcommand("end") {
                booleanArgument("目標を達成したか")
                playerExecutor { _, args ->
                    if (!GameManager.started) return@playerExecutor
                    GameManager.end(args[0] as Boolean)
                    plugin.server.broadcast(Component.text("${Plugin.prefix} ゲームが終了されました"))
                }
            }
            subcommand("start") {
                stringArgument("目標") {
                    replaceSuggestions(ArgumentSuggestions.strings { ManhuntMission.values().map { it.name }.toTypedArray() })
                }
                playerExecutor { player, args ->
                    if (GameManager.started) {
                        player.sendMessage("$prefix ゲームは既に開始されています")
                        return@playerExecutor
                    }

                    val rule = ManhuntMission.valueOf(args[0] as String)
                    GameManager.prepare(player, rule)
                    plugin.server.broadcast(Component.text("$prefix ゲームが開始されました"))
                }
            }
            subcommand("group") {
                integerArgument("人数")
                playerExecutor { player, args ->
                    GameManager.grouping(args[0] as Int)
                    player.sendMessage("$prefix ランナーとハンターをグルーピングしました")
                }
            }
        }

        fun getPlayerName(uuid: UUID): String = transaction {
            (NameRecord.select { NameRecord.uuid eq uuid }.singleOrNull() ?: return@transaction "")[NameRecord.ign]
        }

        fun playerArgSuggest() = CompletableFuture.supplyAsync {
            transaction { Users.selectAll().map { getPlayerName(it[Users.uuid]) }.toSet().toTypedArray() }
        }.get()

        commandAPICommand("manhuntstatstics") {
            withAliases("mhstats")
            offlinePlayerArgument("プレイヤー") {
                replaceSuggestions(
                    ArgumentSuggestions.strings { playerArgSuggest() }
                )
            }
            playerExecutor { player, args ->
                StatisticsScreen.openStatistics(player, args[0] as OfflinePlayer, Teams.HUNTER)
            }
        }

        commandAPICommand("manhuntstatstics") {
            withAliases("mhstats")
            offlinePlayerArgument("プレイヤー") {
                replaceSuggestions(
                    ArgumentSuggestions.strings { playerArgSuggest() }
                )
            }
            greedyStringArgument("日付") {
                replaceSuggestions(
                    ArgumentSuggestions.strings { info ->
                        CompletableFuture.supplyAsync {
                            transaction { Users.select { Users.uuid eq (info.previousArgs[0] as OfflinePlayer).uniqueId }.map { "${it[Users.date]}" }.toTypedArray() }
                        }.get()
                    }
                )
            }
            playerExecutor { player, args ->
                transaction {
                    val date = LocalDateTime.parse(args[1] as String)
                    val selector = Users.select { (Users.uuid eq (args[0] as OfflinePlayer).uniqueId) and (Users.date eq date) }.single()
                    StatisticsScreen.openStatistics(player, args[0] as OfflinePlayer, selector[Users.team], date)
                }
            }
        }

        commandAPICommand("manhuntmatch") {
            withAliases("mhmatch")
            greedyStringArgument("日付") {
                replaceSuggestions(
                    ArgumentSuggestions.strings { info ->
                        CompletableFuture.supplyAsync {
                            transaction { Users.select { Users.uuid eq (info.previousArgs[0] as OfflinePlayer).uniqueId }.map { "${it[Users.date]}" }.toTypedArray() }
                        }.get()
                    }
                )
            }
            playerExecutor { player, anies ->
                transaction {
                    val date = LocalDateTime.parse(anies[0] as String)
                    val matchData = Matches.select { Matches.date eq date }.single()

                    player.sendMessage(
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
        }

        commandAPICommand("toggleglobalchat") {
            withAliases("togglegc")
            playerExecutor { player, anies ->
                if (player.hasMetadata("mh_globalchat")) {
                    player.removeMetadata("mh_globalchat", this@Plugin)
                } else {
                    player.setMetadata("mh_globalchat", FixedMetadataValue(this@Plugin, true))
                }
            }
        }
    }
}

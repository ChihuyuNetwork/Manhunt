package love.chihuyu

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import love.chihuyu.database.Matches
import love.chihuyu.database.NameRecord
import love.chihuyu.database.Users
import love.chihuyu.game.*
import love.chihuyu.game.GameManager.hunters
import love.chihuyu.game.GameManager.runners
import love.chihuyu.gui.StatisticsScreen
import love.chihuyu.utils.CompassUtil
import love.chihuyu.utils.ItemUtil
import love.chihuyu.utils.runTaskLater
import love.chihuyu.utils.runTaskTimer
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Criterias
import org.bukkit.scoreboard.DisplaySlot
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
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
            this,
            MissionChecker,
            EventCanceller,
            StatisticsCollector,
            StatisticsScreen,
            VanillaTweaker
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
        commandAPICommand("manhunt") {
            withAliases("mh")
            withPermission(CommandPermission.OP)
            subcommand("end") {
                argument(BooleanArgument("目標を達成したか"))
                playerExecutor { _, args ->
                    if (!GameManager.started) return@playerExecutor
                    GameManager.end(args[0] as Boolean)
                    plugin.server.broadcast(Component.text("${Plugin.prefix} ゲームが終了されました"))
                }
            }
            subcommand("start") {
                argument(StringArgument("目標").replaceSuggestions(ArgumentSuggestions.strings { ManhuntMission.values().map { it.name }.toTypedArray() }))
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
                argument(IntegerArgument("人数"))
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
            argument(OfflinePlayerArgument("プレイヤー").replaceSuggestions(
                ArgumentSuggestions.strings { playerArgSuggest() }
            ))
            playerExecutor { player, args ->
                StatisticsScreen.openStatistics(player, args[0] as OfflinePlayer, Teams.HUNTER)
            }
        }

        commandAPICommand("manhuntstatstics") {
            withAliases("mhstats")
            argument(OfflinePlayerArgument("プレイヤー").replaceSuggestions(
                ArgumentSuggestions.strings { playerArgSuggest() }
            ))
            argument(GreedyStringArgument("日付").replaceSuggestions(ArgumentSuggestions.strings { info ->
                CompletableFuture.supplyAsync {
                    transaction { Users.select { Users.uuid eq (info.previousArgs[0] as OfflinePlayer).uniqueId }.map { "${it[Users.date]}" }.toTypedArray() }
                }.get()
            }))
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
            argument(GreedyStringArgument("日付").replaceSuggestions(ArgumentSuggestions.stringsAsync {
                CompletableFuture.supplyAsync {
                    transaction { Matches.selectAll().map { "${it[Matches.date]}" }.toTypedArray() }
                }
            }))
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
    }

    @EventHandler
    fun onMine(e: BlockBreakEvent) {
        if (e.block.type in listOf(
                Material.COAL_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.GOLD_ORE,
                Material.IRON_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.NETHER_GOLD_ORE,
                Material.NETHER_QUARTZ_ORE,
            )
        )
            e.expToDrop += 24
    }

    @EventHandler
    fun onRespawn(e: PlayerPostRespawnEvent) {
        val player = e.player

        ItemUtil.giveCompassIfNone(player)
        player.teleport(player.killer?.location ?: player.bedSpawnLocation ?: player.world.spawnLocation)
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.entity

        e.drops.removeIf { it.type == Material.COMPASS }
        if (player in runners()) {
            player.gameMode = GameMode.SPECTATOR
        }

    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        transaction {
            addLogger(StdOutSqlLogger)
            if (NameRecord.select { NameRecord.uuid eq player.uniqueId }.count() == 0L) {
                NameRecord.insert {
                    it[this.uuid] = player.uniqueId
                    it[this.ign] = player.name
                }
            } else {
                NameRecord.update({ NameRecord.uuid eq player.uniqueId }) {
                    it[this.ign] = player.name
                }
            }
        }

        if (player !in hunters() && player !in runners()) {
            GameManager.board.getTeam(Teams.HUNTER.teamName)?.addPlayer(player)
        }

        val obj = GameManager.board.getObjective("health") ?: GameManager.board.registerNewObjective(
            "health",
            Criterias.HEALTH,
            Component.text("${ChatColor.RED}♥")
        )
        obj.displaySlot = DisplaySlot.BELOW_NAME

        player.scoreboard = GameManager.board
        player.gameMode =
            if (player.gameMode == GameMode.SPECTATOR) {
                GameMode.SPECTATOR
            } else {
                GameMode.SURVIVAL
            }
        ItemUtil.giveCompassIfNone(player)
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))

        if (Instant.now().epochSecond - GameManager.startEpoch < 30 && GameManager.started && player in hunters()) {
            GameManager.frozen.add(player)
        }
    }

    @EventHandler
    fun onHit(e: ProjectileHitEvent) {
        if (e.hitEntity is Player && e.entity is Snowball) (e.hitEntity as Player).damage(0.1)
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = e.player
        val isGlobal = e.message.startsWith('!')

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
                in hunters() -> "$teamColor[HUNT]${ChatColor.RESET}"
                in runners() -> "$teamColor[RUN]${ChatColor.RESET}"
                else -> "$teamColor[NULL]${ChatColor.RESET}"
            }

        e.recipients.removeIf { !isGlobal && !(GameManager.board.getPlayerTeam(player)?.hasPlayer(it) ?: true) }

        if (isGlobal) e.message = e.message.substringAfter('!')
        e.format = "$teamPrefix ${player.name}: ${e.message}"
    }

    @EventHandler
    fun compassTracker(e: PlayerInteractEvent) {
        val player = e.player
        val action = e.action
        val item = e.item ?: return
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return
        if (item.type != Material.COMPASS) return

        val nextPlayer = try {
            val other = plugin.server.onlinePlayers.toList().minus(player).filter { it.gameMode == GameMode.SURVIVAL }
            other[(other.indexOf(compassTargets[player]) + (if (player.isSneaking) -1 else 1)) % other.size]
        } catch (e: Exception) {
            return
        }
        compassTargets[player] = nextPlayer

        CompassUtil.setTargetTo(player, nextPlayer)
    }

    @EventHandler
    fun updateCompassTracking(e: PlayerMoveEvent) {
        val player = e.player
        if (player in cooltimed) return
        compassTargets.filter { it.value == player }.forEach { (hunter, target) ->
            CompassUtil.setTargetTo(hunter, target)
        }

        cooltimed.add(player)

        plugin.runTaskLater(20) { cooltimed.remove(player) }
    }
}

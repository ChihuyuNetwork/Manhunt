package love.chihuyu.game

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.database.Matches
import love.chihuyu.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Team
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.ceil

object GameManager {

    val board = plugin.server.scoreboardManager.mainScoreboard
    val frozen = mutableListOf<OfflinePlayer>()

    fun hunters() = plugin.server.onlinePlayers.filter { board.getPlayerTeam(it)?.name == Teams.HUNTER.teamName }.toSet()
    fun runners() = plugin.server.onlinePlayers.filter { board.getPlayerTeam(it)?.name == Teams.RUNNER.teamName }.toSet()

    var started: Boolean = false
    lateinit var taskTickGame: BukkitTask
    lateinit var taskTickEnd: BukkitTask
    lateinit var mission: ManhuntMission

    var endEpoch = EpochUtil.nowEpoch()
    var startEpoch = EpochUtil.nowEpoch()

    internal fun grouping(escapers: Int) {
        board.teams.forEach(Team::unregister)

        val hunterTeam = board.registerNewTeam(Teams.HUNTER.teamName)
        val runnerTeam = board.registerNewTeam(Teams.RUNNER.teamName)

        hunterTeam.color(NamedTextColor.WHITE)
        runnerTeam.color(NamedTextColor.RED)

        hunters().forEach { hunterTeam.removePlayer(it) }
        runners().forEach { runnerTeam.removePlayer(it) }

        repeat(escapers) {
            runnerTeam.addPlayer(plugin.server.onlinePlayers.minus(runners()).random())
        }

        plugin.server.onlinePlayers.minus(runners()).forEach {
            hunterTeam.addPlayer(it)
        }
    }

    internal fun prepare(sender: Player, mission: ManhuntMission) {
        GameManager.mission = mission

        plugin.server.worlds.forEach {
            it.time = 1000
        }

        var error = ""
        when {
            hunters().isEmpty() || runners().isEmpty() -> {
                grouping(ceil(plugin.server.onlinePlayers.size / 2.5).toInt())
                error = "ハンターもしくはランナーのチームが空だっただめ、再割り振りしました"
            }

            plugin.server.onlinePlayers.any { board.getPlayerTeam(it) == null } -> {
                plugin.server.onlinePlayers.filter { board.getPlayerTeam(it) == null }.forEach {
                    board.getTeam(Teams.HUNTER.teamName)?.addPlayer(it)
                }
                error = "ハンターもしくはランナーのチームに所属していないプレイヤーがいたため、ハンターに割り振りました"
            }

            board.getTeam(Teams.HUNTER.teamName) == null || board.getTeam(Teams.RUNNER.teamName) == null -> {
                grouping(ceil(plugin.server.onlinePlayers.size / 2.5).toInt())
                error = "ハンターもしくはランナーのチームがなかったため、再割り振りしました"
            }
        }

        if (error.isNotEmpty()) sender.sendMessage("$prefix $error")

        var remainCountdown = 5

        val countdown = plugin.runTaskTimer(0, 20) {
            plugin.server.onlinePlayers.forEach {
                it.showTitle(
                    Title.title(
                        Component.text("${ChatColor.BOLD}$remainCountdown"),
                        Component.empty(),
                        Title.Times.times(
                            Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(0)
                        )
                    )
                )

                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }

            remainCountdown--
        }

        plugin.runTaskLater(5 * 20) {
            countdown.cancel()
            start(mission)
        }
    }

    private fun start(mission: ManhuntMission) {
        started = true

        plugin.server.onlinePlayers.forEach {
            it.showTitle(
                Title.title(
                    Component.text("${ChatColor.GREEN}${ChatColor.BOLD}${ChatColor.ITALIC}ゲームスタート！"),
                    Component.text(mission.msg),
                    Title.Times.times(
                        Duration.ofSeconds(1), Duration.ofSeconds(7), Duration.ofSeconds(1)
                    )
                )
            )

            it.gameMode = GameMode.SURVIVAL
            it.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false, false))
            it.playSound(it.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            ItemUtil.giveCompassIfNone(it)
        }

        startEpoch = EpochUtil.nowEpoch()
        endEpoch = Instant.now().plus(Duration.ofHours(mission.hour)).epochSecond

        taskTickGame = plugin.runTaskTimer(0, 20) {
            BossbarUtil.updateBossbar(mission)
        }

        taskTickEnd = plugin.runTaskLater((endEpoch - startEpoch) * 20L) {
            end(false)
        }

        hunters().forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 1200, 0, true, false, true))
            frozen.add(it)
        }

        plugin.runTaskLater(1200) {
            frozen.clear()
        }

        StatisticsCollector.onGameStart()
    }

    internal fun end(missioned: Boolean) {
        val startLocalDate = LocalDateTime.ofEpochSecond(startEpoch, 0, ZoneId.of("Asia/Tokyo").rules.getOffset(Instant.ofEpochSecond(startEpoch)))
        started = false

        taskTickGame.cancel()
        taskTickEnd.cancel()

        plugin.server.onlinePlayers.forEach {
            it.showTitle(
                Title.title(
                    Component.text("${ChatColor.RED}${ChatColor.BOLD}${ChatColor.ITALIC}ゲームオーバー！"),
                    Component.text(if (missioned) "ランナーの勝ち" else "ハンターの勝ち"),
                    Title.Times.times(
                        Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1)
                    )
                )
            )
        }

        transaction {
            addLogger(StdOutSqlLogger)
            Matches.insert {
                it[this.date] = startLocalDate
                it[this.seed] = plugin.server.getWorld("world")?.seed ?: 0L
                it[this.matchTime] = Instant.now().epochSecond - startEpoch
                it[this.winnerTeam] = if (missioned) Teams.RUNNER else Teams.HUNTER
                it[this.mission] = this@GameManager.mission
            }
        }

        StatisticsCollector.onGameEnd()
        StatisticsCollector.collect(startLocalDate)
        StatisticsCollector.clear()
    }
}
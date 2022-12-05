package love.chihuyu.game

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Team
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

object GameManager {

    private const val hunterTeamName = "hunter"
    private const val runnerTeamName = "runner"
    val board = plugin.server.scoreboardManager.mainScoreboard

    fun hunters() = plugin.server.onlinePlayers.filter { board.getPlayerTeam(it)?.name == hunterTeamName }.toSet()
    fun runners() = plugin.server.onlinePlayers.filter { board.getPlayerTeam(it)?.name == runnerTeamName }.toSet()

    var started: Boolean = false
    lateinit var taskTickGame: BukkitTask
    lateinit var taskTickEnd: BukkitTask
    lateinit var mission: ManhuntMission

    var endEpoch = EpochUtil.nowEpoch()
    var startEpoch = EpochUtil.nowEpoch()

    internal fun grouping(escapers: Int) {
        board.teams.forEach(Team::unregister)

        val hunterTeam = board.registerNewTeam(hunterTeamName)
        val runnerTeam = board.registerNewTeam(runnerTeamName)

        hunterTeam.color(NamedTextColor.GOLD)
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

    internal fun prepare(sender: CommandSender, mission: ManhuntMission) {
        GameManager.mission = mission

        var error = ""
        when {
            hunters().isEmpty() || runners().isEmpty() -> {
                grouping(ceil(plugin.server.onlinePlayers.size / 2.5).toInt())
                error = "ハンターもしくはランナーのチームが空だっただめ、再割り振りしました"
            }
            plugin.server.onlinePlayers.any { board.getPlayerTeam(it) == null } -> {
                plugin.server.onlinePlayers.filter { board.getPlayerTeam(it) == null }.forEach {
                    board.getTeam(hunterTeamName)?.addPlayer(it)
                }
                error = "ハンターもしくはランナーのチームに所属していないプレイヤーがいたため、ハンターに割り振りました"
            }
            board.getTeam(hunterTeamName) == null || board.getTeam(runnerTeamName) == null -> {
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
                        Component.text(
                            "${ChatColor.BOLD}" + "> ".repeat(remainCountdown) + remainCountdown + " <".repeat(
                                remainCountdown
                            )
                        ),
                        Component.empty(),
                        Title.Times.of(
                            Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(0)
                        )
                    )
                )

                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)

                it.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 60 * 20, 254, false, false))
                it.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 254, false, false))
            }

            hunters().forEach {
                it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 60 * 20, 254, false, false))
                it.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 60 * 20, 254, false, false))
                it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 60 * 20, 254, false, false))
                it.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 60 * 20, 136, false, false))
            }

            remainCountdown--
        }

        plugin.runTaskLater(5 * 20) {
            countdown.cancel()

            plugin.server.onlinePlayers.forEach {
                it.showTitle(
                    Title.title(
                        Component.text("${ChatColor.GREEN}${ChatColor.BOLD}${ChatColor.ITALIC}ゲームスタート！"),
                        Component.text(mission.msg),
                        Title.Times.of(
                            Duration.ofSeconds(1), Duration.ofSeconds(7), Duration.ofSeconds(1)
                        )
                    )
                )

                it.gameMode = GameMode.ADVENTURE
                it.playSound(it.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            }

            start(mission)
        }
    }

    private fun start(mission: ManhuntMission) {
        started = true

        startEpoch = EpochUtil.nowEpoch()
        endEpoch = Instant.now().plus(Duration.ofHours(mission.hour)).epochSecond

        taskTickGame = plugin.runTaskTimer(0, 20) {
            BossbarUtil.updateBossbar()
        }

        taskTickEnd = plugin.runTaskLater((endEpoch - startEpoch) * 20L) {
            end(false)
        }

        plugin.server.onlinePlayers.forEach {
            it.gameMode = GameMode.SURVIVAL
            ItemUtil.giveCompassIfNone(it)
        }
    }

    internal fun end(missioned: Boolean) {
        started = false

        taskTickGame.cancel()
        taskTickEnd.cancel()

        plugin.server.onlinePlayers.forEach {
            it.showTitle(
                Title.title(
                    Component.text("${ChatColor.RED}${ChatColor.BOLD}${ChatColor.ITALIC}ゲームオーバー！"),
                    Component.text(if (missioned) "ランナーの勝ち" else "ハンターの勝ち"),
                    Title.Times.of(
                        Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1)
                    )
                )
            )

            it.gameMode = GameMode.ADVENTURE
        }
    }
}
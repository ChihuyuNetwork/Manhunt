package love.chihuyu.game

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.utils.BossbarUtil
import love.chihuyu.utils.EpochUtil
import love.chihuyu.utils.runTaskLater
import love.chihuyu.utils.runTaskTimer
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant

object GameManager {

    val hunterTeamName = "hunter"
    val escaperTeamName = "escaper"
    val board = plugin.server.scoreboardManager.mainScoreboard

    fun hunters() = plugin.server.onlinePlayers.filter { board.getPlayerTeam(it)?.name == hunterTeamName }.toSet()
    fun escapers() = plugin.server.onlinePlayers.filter { board.getPlayerTeam(it)?.name == escaperTeamName }.toSet()

    var started: Boolean = false
    lateinit var taskTickGame: BukkitTask
    lateinit var taskTickEnd: BukkitTask
    lateinit var mission: ManhuntMission

    var endEpoch = EpochUtil.nowEpoch()
    var startEpoch = EpochUtil.nowEpoch()

    fun prepare(mission: ManhuntMission) {
        GameManager.mission = mission

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
                        Title.Times.times(
                            Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1)
                        )
                    )
                )

                it.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 60 * 20, 254, false, false))
                it.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 254, false, false))
            }

            hunters().forEach {
                it.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 60 * 20, 254, false, false))
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
                        Component.empty(),
                        Title.Times.times(
                            Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1)
                        )
                    )
                )

                it.gameMode = GameMode.ADVENTURE
                it.playSound(it, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            }

            start(mission)
        }
    }

    private fun start(mission: ManhuntMission) {
        started = true

        startEpoch = EpochUtil.nowEpoch()
        endEpoch = Instant.now().plus(Duration.ofHours(mission.hour)).epochSecond

        taskTickGame = plugin.runTaskTimer(0, 20) {
            BossbarUtil.updateBossbar(NamespacedKey(plugin, "manhunt"))
        }

        taskTickEnd = plugin.runTaskLater((endEpoch - startEpoch) * 20L) {
            end(false)
        }

        plugin.server.onlinePlayers.forEach {
            it.gameMode = GameMode.SURVIVAL
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
                    Component.text(if (missioned) "マンの勝ち" else "ハンターの勝ち"),
                    Title.Times.times(
                        Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1)
                    )
                )
            )

            it.gameMode = GameMode.ADVENTURE
        }
    }
}
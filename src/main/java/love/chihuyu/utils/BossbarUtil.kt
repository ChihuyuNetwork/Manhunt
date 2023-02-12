package love.chihuyu.utils

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.game.GameManager
import love.chihuyu.game.ManhuntMission
import love.chihuyu.utils.TeamUtil.isHunter
import love.chihuyu.utils.TeamUtil.isRunner
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle

object BossbarUtil {

    fun updateBossbar(mission: ManhuntMission) {
        plugin.server.onlinePlayers.forEach {
            val key = NamespacedKey(plugin, if (it.isRunner()) "manhunt-runner" else "manhunt-hunter")
            val bossbar = plugin.server.getBossBar(key) ?: Bukkit.createBossBar(key, "残り時間", BarColor.RED, BarStyle.SOLID)
            val remains = GameManager.endEpoch - EpochUtil.nowEpoch()
            val msg = if (it.isHunter()) "ランナーを全員殺せ" else mission.msg

            bossbar.setTitle("$msg ≫ ${EpochUtil.formatTime(remains)}")
            bossbar.isVisible = true
            bossbar.progress = (remains * (1.0 / (GameManager.endEpoch - GameManager.startEpoch))).coerceIn(.0..1.0)

            bossbar.addPlayer(it)
        }
    }
}

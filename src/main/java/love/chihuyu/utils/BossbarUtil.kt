package love.chihuyu.utils

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.game.GameManager
import love.chihuyu.game.ManhuntMission
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle

object BossbarUtil {

    fun updateBossbar(mission: ManhuntMission) {
        plugin.server.onlinePlayers.forEach {
            val key = NamespacedKey(plugin, "manhunt-${it.uniqueId}")
            val bossbar = Bukkit.getBossBar(key) ?: Bukkit.createBossBar(key, "残り時間", BarColor.RED, BarStyle.SOLID)
            val remains = GameManager.endEpoch - EpochUtil.nowEpoch()
            val msg = if (it in GameManager.hunters()) "ランナーを全員殺せ" else mission.msg

            bossbar.setTitle("$msg ≫ ${EpochUtil.formatTime(remains)}")
            bossbar.isVisible = true
            bossbar.removeAll()
            bossbar.progress = remains * +(1.0 / (GameManager.endEpoch - GameManager.startEpoch))
            bossbar.addPlayer(it)
        }
    }
}

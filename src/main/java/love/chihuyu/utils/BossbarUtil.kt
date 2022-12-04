package love.chihuyu.utils

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.game.GameManager
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle

object BossbarUtil {

    fun updateBossbar(key: NamespacedKey) {
        val bossbar = Bukkit.getBossBar(key) ?: Bukkit.createBossBar(key, "残り時間", BarColor.RED, BarStyle.SOLID)
        val remains = GameManager.endEpoch - EpochUtil.nowEpoch()

        bossbar.setTitle("残り時間: " + EpochUtil.formatTime(remains))
        bossbar.isVisible = true
        bossbar.removeAll()
        plugin.server.onlinePlayers.forEach { bossbar.addPlayer(it) }
        bossbar.progress = remains * +(1.0 / (GameManager.endEpoch - GameManager.startEpoch))
    }
}

package love.chihuyu.listener

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import love.chihuyu.game.GameManager
import love.chihuyu.game.Teams
import love.chihuyu.utils.ItemUtil
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Criterias
import org.bukkit.scoreboard.DisplaySlot
import java.time.Instant

object GameListener: Listener {

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
        if (player in GameManager.runners()) {
            player.gameMode = GameMode.SPECTATOR
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        if (player !in GameManager.hunters() && player !in GameManager.runners()) {
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

        if (Instant.now().epochSecond - GameManager.startEpoch < 30 && GameManager.started && player in GameManager.hunters()) {
            GameManager.frozen.add(player)
        }
    }

    @EventHandler
    fun onHit(e: ProjectileHitEvent) {
        if (e.hitEntity is Player && e.entity is Snowball) (e.hitEntity as Player).damage(0.1)
    }
}
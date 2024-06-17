package love.chihuyu

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import love.chihuyu.game.*
import love.chihuyu.listener.CompassTracker
import love.chihuyu.listener.GameListener
import love.chihuyu.listener.TeamChatter
import love.chihuyu.utils.runTaskTimer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var compassTask: BukkitTask
        var cooltimed = mutableSetOf<Player>()
        val prefix = "${NamedTextColor.GOLD}[MH]${NamedTextColor.WHITE}"
        val compassTargets = mutableMapOf<Player, Player>()
    }
    
    init {
        plugin = this
    }

    override fun onEnable() {
        initCommands()

        listOf(
            MissionChecker,
            EventCanceller,
            VanillaTweaker,
            CompassTracker,
            GameListener,
            TeamChatter
        ).forEach { server.pluginManager.registerEvents(it, this) }

        compassTask = runTaskTimer(0, 0) {
            server.onlinePlayers.forEach {
                val target = compassTargets[it]
                it.sendActionBar(Component.text("${NamedTextColor.WHITE}追跡中 ≫ " + target?.name))
            }
        }

        RecipeManager.add()
    }

    override fun onDisable() {
        compassTask.cancel()
    }

    private fun initCommands() {
        commandAPICommand("manhunt") {
            permission = CommandPermission.OP
            withAliases("mh")
            subcommand("end") {
                booleanArgument("isCompletedObjective")
                playerExecutor { _, args ->
                    if (!GameManager.started) return@playerExecutor
                    GameManager.end(args[0] as Boolean)
                    plugin.server.broadcast(Component.text("${Plugin.prefix} ゲームが終了されました"))
                }
            }
            subcommand("start") {
                stringArgument("objective") {
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

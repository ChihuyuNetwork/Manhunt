package love.chihuyu.commands

import love.chihuyu.Plugin
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.game.GameManager
import love.chihuyu.game.ManhuntMission
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandManhunt: Command("manhunt") {
    override fun onCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (args.isEmpty() || !sender.isOp || sender !is Player) return

        when (args[0]) {
            "group" -> {
                if (args.size < 2) return
                GameManager.grouping(Integer.parseInt(args[1]))

                sender.sendMessage("${Plugin.prefix} マンとハンターをグルーピングしました")
            }
            "start" -> {
                if (args.size < 2) return
                val rule = ManhuntMission.valueOf(args[1])
                GameManager.prepare(sender, rule)

                plugin.server.broadcast(Component.text("${Plugin.prefix} ゲームが開始されました"))
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("group", "start")
            2 -> when (args[0]) {
                "group" -> listOf()
                "start" -> ManhuntMission.values().map { it.name }
                else -> listOf()
            }
            else -> listOf()
        }
    }
}
package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.game.GameManager
import love.chihuyu.game.ManhuntMission
import net.kyori.adventure.text.Component

object ManhuntStart {

    val main: CommandAPICommand = CommandAPICommand("start")
        .withArguments(StringArgument("mission").replaceSuggestions(ArgumentSuggestions.strings(ManhuntMission.values().map { it.name })))
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                if (GameManager.started) {
                    sender.sendMessage("$prefix ゲームは既に開始されています")
                    return@PlayerCommandExecutor
                }

                val rule = ManhuntMission.valueOf(args[0] as String)
                GameManager.prepare(sender, rule)
                plugin.server.broadcast(Component.text("$prefix ゲームが開始されました"))
            }
        )
}
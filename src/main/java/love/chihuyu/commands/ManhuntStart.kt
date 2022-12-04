package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.CommandExecutor
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.game.GameManager
import love.chihuyu.rules.ManhuntMission

object ManhuntStart {

    val main = CommandAPICommand("start")
        .withArguments(
            MultiLiteralArgument(*ManhuntMission.values().map { it.name }.toTypedArray())
                .replaceSuggestions(ArgumentSuggestions.strings(*ManhuntMission.values().map { it.name }.toTypedArray()))
        )
        .executes(
            CommandExecutor { sender, args ->
                val rule = ManhuntMission.valueOf(args[0] as String)
                GameManager.prepare(rule)

                sender.sendMessage("$prefix Game started.")
            }
        )
}

package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import love.chihuyu.Plugin
import love.chihuyu.game.GameManager

object ManhuntGroup {

    val main: CommandAPICommand = CommandAPICommand("group")
        .withArguments(IntegerArgument("runners"))
        .executes(
            CommandExecutor { sender, args ->
                GameManager.grouping(args[0] as Int)
                sender.sendMessage("${Plugin.prefix} ランナーとハンターをグルーピングしました")
            }
        )
}

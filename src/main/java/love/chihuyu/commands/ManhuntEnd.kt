package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.BooleanArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.Plugin
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.game.GameManager
import net.kyori.adventure.text.Component

object ManhuntEnd {

    val main: CommandAPICommand = CommandAPICommand("end")
        .withArguments(BooleanArgument("missioned"))
        .executesPlayer(
            PlayerCommandExecutor { _, args ->
                if (!GameManager.started) return@PlayerCommandExecutor
                GameManager.end(args[0] as Boolean)
                plugin.server.broadcast(Component.text("${Plugin.prefix} ゲームが終了されました"))
            }
        )
}
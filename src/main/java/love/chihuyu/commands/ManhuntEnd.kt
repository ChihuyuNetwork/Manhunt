package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.Plugin
import love.chihuyu.game.GameManager
import net.kyori.adventure.text.Component

object ManhuntEnd {

    val main = CommandAPICommand("end")
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            GameManager.end(false)
            Plugin.plugin.server.broadcast(Component.text("${Plugin.prefix} ゲームが終了されました"))
        })
}
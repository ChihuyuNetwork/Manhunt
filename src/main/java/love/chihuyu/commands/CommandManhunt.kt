package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission

object CommandManhunt {
    val main: CommandAPICommand = CommandAPICommand("manhunt")
        .withAliases("mh")
        .withPermission(CommandPermission.OP)
        .withSubcommands(
            ManhuntGroup.main,
            ManhuntEnd.main,
            ManhuntStart.main
        )
}

package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission

object CommandManhunt {

    val main = CommandAPICommand("manhunt")
        .withAliases("mh")
        .withPermission(CommandPermission.OP)
        .withSubcommands(
            ManhuntStart.main,
            ManhuntGroup.main
        )
}

package love.chihuyu.gui

import love.chihuyu.game.Teams
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.time.LocalDateTime

class ScreenStatus(val opener: Player, val target: OfflinePlayer, val team: Teams, val inventory: Inventory, val dateTime: LocalDateTime? = null)
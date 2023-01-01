package love.chihuyu.gui

import love.chihuyu.database.NameRecord
import love.chihuyu.database.Users
import love.chihuyu.game.Teams
import love.chihuyu.utils.EpochUtil
import love.chihuyu.utils.ItemUtil
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object StatisticsScreen : Listener {

    private val screenStatuses = mutableListOf<ScreenStatus>()

    fun openStatistics(player: Player, target: OfflinePlayer, team: Teams) {
        val localizedTeamName = when (team) {
            Teams.HUNTER -> "ハンター"
            Teams.RUNNER -> "ランナー"
        }
        val inv = Bukkit.createInventory(
            player, 45,
            Component.text(
                "${ChatColor.BOLD}${transaction { NameRecord.select { NameRecord.uuid eq target.uniqueId }.singleOrNull()
                    ?.get(NameRecord.ign) ?: "" }} / ${localizedTeamName}成績"
            )
        )
        transaction {
            val statisticsData = Users.select { (Users.uuid eq target.uniqueId) and (Users.team eq team) }
            var plays = Users.select { (Users.uuid eq target.uniqueId) and (Users.team eq team) }.count()

            if (plays == 0L) plays = 1

            inv.setItem(
                10,
                ItemUtil.create(
                    Material.OAK_BOAT, name = "${ChatColor.GOLD}${ChatColor.BOLD}移動",
                    lore = listOf(
                        "${ChatColor.WHITE}平均エンド到達時間: ${EpochUtil.formatTime(statisticsData.sumOf { it[Users.timeToTheEnd] } / plays)}",
                        "${ChatColor.WHITE}平均ネザー到達時間: ${EpochUtil.formatTime(statisticsData.sumOf { it[Users.timeToNether] } / plays)}",
                        "${ChatColor.WHITE}平均移動距離: ${"%.2f".format(statisticsData.sumOf { it[Users.traveled].toDouble() } / 100 / plays.toDouble())}m"
                    ).plus(
                        if (team == Teams.RUNNER) {
                            "${ChatColor.WHITE}平均生存時間: ${EpochUtil.formatTime(statisticsData.sumOf { it[Users.aliveTime] } / plays)}"
                        } else ""
                    )
                )
            )

            inv.setItem(
                11,
                ItemUtil.create(
                    Material.IRON_SWORD, name = "${ChatColor.GOLD}${ChatColor.BOLD}PvP",
                    lore = listOf(
                        "${ChatColor.WHITE}デス率: ${"%.2f".format(statisticsData.sumOf { it[Users.deathes].toDouble() } / plays.toDouble())}",
                        "${ChatColor.WHITE}キル率: ${"%.2f".format(statisticsData.sumOf { it[Users.playersKilled].toDouble() } / plays.toDouble())}",
                        "${ChatColor.WHITE}弓命中率: ${"%.2f".format(statisticsData.sumOf { it[Users.arrowHitted].toDouble() } / statisticsData.sumOf { it[Users.arrowShooted].toDouble() })}"
                    )
                )
            )

            inv.setItem(
                12,
                ItemUtil.create(
                    Material.PIG_SPAWN_EGG, name = "${ChatColor.GOLD}${ChatColor.BOLD}モブ",
                    lore = listOf(
                        "${ChatColor.WHITE}全体キル率: ${"%.2f".format(statisticsData.sumOf { it[Users.mobsKilled].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}エンダーマンキル率: ${"%.2f".format(statisticsData.sumOf { it[Users.endermansKilled].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}ブレイズキル率: ${"%.2f".format(statisticsData.sumOf { it[Users.blazesKilled].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}ゴーレムキル率: ${"%.2f".format(statisticsData.sumOf { it[Users.golemsKilled].toDouble() } / plays.toDouble())}回"
                    )
                )
            )

            inv.setItem(
                13,
                ItemUtil.create(
                    Material.STONE, name = "${ChatColor.GOLD}${ChatColor.BOLD}ブロック",
                    lore = listOf(
                        "${ChatColor.WHITE}破壊率: ${"%.2f".format(statisticsData.sumOf { it[Users.blocksBroken].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}設置率: ${"%.2f".format(statisticsData.sumOf { it[Users.blocksPlaced].toDouble() } / plays.toDouble())}回"
                    )
                )
            )

            inv.setItem(
                14,
                ItemUtil.create(
                    Material.BREAD, name = "${ChatColor.GOLD}${ChatColor.BOLD}アイテム",
                    lore = listOf(
                        "${ChatColor.WHITE}ポーション使用率: ${"%.2f".format(statisticsData.sumOf { it[Users.potionsUsed].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}ガップル使用率: ${"%.2f".format(statisticsData.sumOf { it[Users.gapplesUsed].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}クラフト率: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsCrafted].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}エンチャント率: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsEnchanted].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}修繕率: ${"%.2f".format(statisticsData.sumOf { it[Users.toolsRepaired].toDouble() } / plays.toDouble())}回"
                    )
                )
            )

            inv.setItem(
                15,
                ItemUtil.create(
                    Material.ENDER_PEARL, name = "${ChatColor.GOLD}${ChatColor.BOLD}準備",
                    lore = listOf(
                        "${ChatColor.WHITE}鉄精錬率: ${"%.2f".format(statisticsData.sumOf { it[Users.ironsSmelt].toDouble() } / plays.toDouble())}個",
                        "${ChatColor.WHITE}石炭採掘率: ${"%.2f".format(statisticsData.sumOf { it[Users.coalsMined].toDouble() } / plays.toDouble())}個",
                        "${ChatColor.WHITE}村人交換率: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsTraded].toDouble() } / plays.toDouble())}回",
                        "${ChatColor.WHITE}ピグリン交換率: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsBartered].toDouble() } / plays.toDouble())}回"
                    )
                )
            )

            inv.setItem(
                16,
                ItemUtil.create(
                    Material.GRASS_BLOCK, name = "${ChatColor.GOLD}${ChatColor.BOLD}ワールド",
                    lore = listOf(
                        "${ChatColor.WHITE}ルートチェスト回収率: ${"%.2f".format(statisticsData.sumOf { it[Users.openedLoots].toDouble() } / plays.toDouble())}個",
                        "${ChatColor.WHITE}チャット率: ${"%.2f".format(statisticsData.sumOf { it[Users.chats].toDouble() } / plays.toDouble())}回"
                    )
                )
            )

            inv.setItem(
                31,
                ItemUtil.create(
                    Material.PLAYER_HEAD,
                    name = "${ChatColor.BOLD}${target.name}",
                    lore = listOf(
                        "ゲームプレイ回数: ${Users.select { Users.uuid eq target.uniqueId }.count()}",
                        "${localizedTeamName}プレイ回数: ${Users.select { (Users.uuid eq target.uniqueId) and (Users.team eq team) }.count()}"
                    )
                ).apply {
                    val meta = this.itemMeta as SkullMeta
                    meta.owningPlayer = target
                    this.itemMeta = meta
                }
            )

            inv.setItem(
                44,
                ItemUtil.create(
                    Material.PAPER,
                    name = "${if (team == Teams.HUNTER) ChatColor.GREEN else ChatColor.RESET}ハンター${ChatColor.GRAY}/${ChatColor.RESET}${if (team == Teams.RUNNER) ChatColor.GREEN else ChatColor.RESET}ランナー"
                )
            )

            screenStatuses += ScreenStatus(player, target, team, inv)
            player.openInventory(inv)
        }
    }

    fun openStatistics(player: Player, target: OfflinePlayer, team: Teams, date: LocalDateTime) {
        val localizedTeamName = when (team) {
            Teams.HUNTER -> "ハンター"
            Teams.RUNNER -> "ランナー"
        }
        val inv = Bukkit.createInventory(
            player, 45,
            Component.text(
                "${ChatColor.BOLD}${transaction { NameRecord.select { NameRecord.uuid eq target.uniqueId }.singleOrNull()
                    ?.get(NameRecord.ign) ?: "" }} / ${localizedTeamName}成績"
            )
        )
        transaction {
            val statisticsData = Users.select { (Users.uuid eq target.uniqueId) and (Users.team eq team) and (Users.date eq date) }
            var plays = Users.select { (Users.uuid eq target.uniqueId) and (Users.team eq team) and (Users.date eq date) }.count()

            if (plays == 0L) plays = 1

            inv.setItem(
                10,
                ItemUtil.create(
                    Material.OAK_BOAT, name = "${ChatColor.GOLD}${ChatColor.BOLD}移動",
                    lore = listOf(
                        "${ChatColor.WHITE}エンド到達時間: ${EpochUtil.formatTime(statisticsData.sumOf { it[Users.timeToTheEnd] })}",
                        "${ChatColor.WHITE}ネザー到達時間: ${EpochUtil.formatTime(statisticsData.sumOf { it[Users.timeToNether] })}",
                        "${ChatColor.WHITE}移動距離: ${"%.2f".format(statisticsData.sumOf { it[Users.traveled].toDouble() } / 100)}m"
                    ).plus(
                        if (team == Teams.RUNNER) {
                            "${ChatColor.WHITE}生存時間: ${EpochUtil.formatTime(statisticsData.sumOf { it[Users.aliveTime] })}"
                        } else {
                            ""
                        }
                    )
                )
            )

            inv.setItem(
                11,
                ItemUtil.create(
                    Material.IRON_SWORD, name = "${ChatColor.GOLD}${ChatColor.BOLD}PvP",
                    lore = listOf(
                        "${ChatColor.WHITE}デス: ${"%.2f".format(statisticsData.sumOf { it[Users.deathes].toDouble() })}",
                        "${ChatColor.WHITE}キル: ${"%.2f".format(statisticsData.sumOf { it[Users.playersKilled].toDouble() })}",
                        "${ChatColor.WHITE}弓命中: ${"%.2f".format(statisticsData.sumOf { it[Users.arrowHitted].toDouble() } / statisticsData.sumOf { it[Users.arrowShooted].toDouble() })}"
                    )
                )
            )

            inv.setItem(
                12,
                ItemUtil.create(
                    Material.PIG_SPAWN_EGG, name = "${ChatColor.GOLD}${ChatColor.BOLD}モブ",
                    lore = listOf(
                        "${ChatColor.WHITE}全体キル: ${"%.2f".format(statisticsData.sumOf { it[Users.mobsKilled].toDouble() })}回",
                        "${ChatColor.WHITE}エンダーマンキル: ${"%.2f".format(statisticsData.sumOf { it[Users.endermansKilled].toDouble() })}回",
                        "${ChatColor.WHITE}ブレイズキル: ${"%.2f".format(statisticsData.sumOf { it[Users.blazesKilled].toDouble() })}回",
                        "${ChatColor.WHITE}ゴーレムキル: ${"%.2f".format(statisticsData.sumOf { it[Users.golemsKilled].toDouble() })}回"
                    )
                )
            )

            inv.setItem(
                13,
                ItemUtil.create(
                    Material.STONE, name = "${ChatColor.GOLD}${ChatColor.BOLD}ブロック",
                    lore = listOf(
                        "${ChatColor.WHITE}破壊: ${"%.2f".format(statisticsData.sumOf { it[Users.blocksBroken].toDouble() })}回",
                        "${ChatColor.WHITE}設置: ${"%.2f".format(statisticsData.sumOf { it[Users.blocksPlaced].toDouble() })}回"
                    )
                )
            )

            inv.setItem(
                14,
                ItemUtil.create(
                    Material.BREAD, name = "${ChatColor.GOLD}${ChatColor.BOLD}アイテム",
                    lore = listOf(
                        "${ChatColor.WHITE}ポーション使用: ${"%.2f".format(statisticsData.sumOf { it[Users.potionsUsed].toDouble() })}回",
                        "${ChatColor.WHITE}ガップル使用: ${"%.2f".format(statisticsData.sumOf { it[Users.gapplesUsed].toDouble() })}回",
                        "${ChatColor.WHITE}クラフト: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsCrafted].toDouble() })}回",
                        "${ChatColor.WHITE}エンチャント: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsEnchanted].toDouble() })}回",
                        "${ChatColor.WHITE}修繕: ${"%.2f".format(statisticsData.sumOf { it[Users.toolsRepaired].toDouble() })}回"
                    )
                )
            )

            inv.setItem(
                15,
                ItemUtil.create(
                    Material.ENDER_PEARL, name = "${ChatColor.GOLD}${ChatColor.BOLD}準備",
                    lore = listOf(
                        "${ChatColor.WHITE}鉄精錬: ${"%.2f".format(statisticsData.sumOf { it[Users.ironsSmelt].toDouble() })}個",
                        "${ChatColor.WHITE}石炭採掘: ${"%.2f".format(statisticsData.sumOf { it[Users.coalsMined].toDouble() })}個",
                        "${ChatColor.WHITE}村人交換: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsTraded].toDouble() })}回",
                        "${ChatColor.WHITE}ピグリン交換: ${"%.2f".format(statisticsData.sumOf { it[Users.itemsBartered].toDouble() })}回"
                    )
                )
            )

            inv.setItem(
                16,
                ItemUtil.create(
                    Material.GRASS_BLOCK, name = "${ChatColor.GOLD}${ChatColor.BOLD}ワールド",
                    lore = listOf(
                        "${ChatColor.WHITE}ルートチェスト回収: ${"%.2f".format(statisticsData.sumOf { it[Users.openedLoots].toDouble() })}個",
                        "${ChatColor.WHITE}チャット: ${"%.2f".format(statisticsData.sumOf { it[Users.chats].toDouble() })}回"
                    )
                )
            )

            inv.setItem(
                31,
                ItemUtil.create(
                    Material.PLAYER_HEAD,
                    name = "${ChatColor.BOLD}${target.name}",
                    lore = listOf(
                        "ゲームプレイ回数: ${Users.select { Users.uuid eq target.uniqueId }.count()}",
                        "${localizedTeamName}プレイ回数: ${Users.select { (Users.uuid eq target.uniqueId) and (Users.team eq team) }.count()}"
                    )
                ).apply {
                    val meta = this.itemMeta as SkullMeta
                    meta.owningPlayer = target
                    this.itemMeta = meta
                }
            )

            inv.setItem(
                36,
                ItemUtil.create(
                    Material.CLOCK,
                    name = "${ChatColor.YELLOW}${ChatColor.BOLD}${ChatColor.ITALIC}$date"
                )
            )

            screenStatuses += ScreenStatus(player, target, team, inv, date)
            player.openInventory(inv)
        }
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        val screenStatus = screenStatuses.firstOrNull { it.inventory == e.clickedInventory } ?: return
        e.isCancelled = true

        if (e.currentItem?.type == Material.PAPER) {
            screenStatuses.remove(screenStatus)
            if (screenStatus.dateTime != null) {
                openStatistics(
                    screenStatus.opener, screenStatus.target,
                    when (screenStatus.team) {
                        Teams.RUNNER -> Teams.HUNTER
                        Teams.HUNTER -> Teams.RUNNER
                    },
                    screenStatus.dateTime
                )
            } else {
                openStatistics(
                    screenStatus.opener, screenStatus.target,
                    when (screenStatus.team) {
                        Teams.RUNNER -> Teams.HUNTER
                        Teams.HUNTER -> Teams.RUNNER
                    }
                )
            }
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        }
    }
}
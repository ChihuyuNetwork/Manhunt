package love.chihuyu.game

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import love.chihuyu.utils.ItemUtil
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EnderSignal
import org.bukkit.entity.Piglin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

object VanillaTweaker : Listener {

    class CustomBartarItem(val weight: Int, val items: List<() -> ItemStack>)

    private val bartarList = listOf(
        CustomBartarItem(
            5,
            listOf
            { ItemUtil.createEnchantBook(storedEnchants = mapOf(Pair(Enchantment.SOUL_SPEED, Random.nextInt(1, 4))), amount = 1) }
        ),
        CustomBartarItem(
            8,
            listOf
            { ItemUtil.create(Material.IRON_BOOTS, enchantments = mapOf(Pair(Enchantment.SOUL_SPEED, Random.nextInt(1, 4))), amount = 1) }
        ),
        CustomBartarItem(
            10,
            listOf(
                { ItemUtil.create(Material.IRON_NUGGET, amount = Random.nextInt(9, 37)) },
                { ItemUtil.createPotion("${ChatColor.RESET}耐火のポーション", customEffects = listOf(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 180 * 20, 0)), amount = 1, color = Color.ORANGE) },
                { ItemUtil.createSplashPotion("${ChatColor.RESET}耐火のスプラッシュポーション", customEffects = listOf(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 180 * 20, 0)), amount = 1, color = Color.ORANGE) }
            )
        ),
        CustomBartarItem(
            20,
            listOf(
                { ItemUtil.create(Material.QUARTZ, amount = Random.nextInt(8, 17)) },
                { ItemUtil.create(Material.GLOWSTONE_DUST, amount = Random.nextInt(5, 13)) },
                { ItemUtil.create(Material.MAGMA_CREAM, amount = Random.nextInt(2, 7)) },
                { ItemUtil.create(Material.ENDER_PEARL, amount = Random.nextInt(4, 9)) },
                { ItemUtil.create(Material.STRING, amount = Random.nextInt(8, 25)) }
            )
        ),
        CustomBartarItem(
            40,
            listOf(
                { ItemUtil.create(Material.FIRE_CHARGE, amount = Random.nextInt(1, 6)) },
                { ItemUtil.create(Material.GRAVEL, amount = Random.nextInt(8, 17)) },
                { ItemUtil.create(Material.LEATHER, amount = Random.nextInt(4, 11)) },
                { ItemUtil.create(Material.NETHER_BRICK, amount = Random.nextInt(4, 17)) },
                { ItemUtil.create(Material.OBSIDIAN, amount = 1) },
                { ItemUtil.create(Material.CRYING_OBSIDIAN, amount = Random.nextInt(1, 4)) },
                { ItemUtil.create(Material.SOUL_SAND, amount = Random.nextInt(4, 17)) }
            )
        )
    ).flatMap { customBartarItem ->
        val list = mutableListOf<() -> ItemStack>()
        repeat(customBartarItem.weight) {
            customBartarItem.items.forEach { list.add(it) }
        }
        return@flatMap list
    }

    @EventHandler
    fun onRespawn(e: PlayerPostRespawnEvent) {
        val player = e.player
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false, false))
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.entity
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false, false))
    }

    @EventHandler
    fun onThrow(e: EntitySpawnEvent) {
        val entity = e.entity
        if (entity !is EnderSignal) return
        entity.dropItem = true
    }

    @EventHandler
    fun onDrop(e: EntityDropItemEvent) {
        val entity = e.entity
        if (entity !is Piglin) return

        e.itemDrop.itemStack = bartarList[Random.nextInt(1, 423)].invoke()
    }
}

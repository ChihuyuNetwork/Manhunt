package love.chihuyu.utils

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect

object ItemUtil {

    fun createSplashPotion(
        name: String? = null,
        localizedName: String? = null,
        amount: Int? = null,
        customModelData: Int? = null,
        lore: List<String>? = null,
        attributeModifier: Map<Attribute, AttributeModifier>? = null,
        enchantments: Map<Enchantment, Int>? = null,
        flags: List<ItemFlag>? = null,
        unbreakable: Boolean? = null,
        customEffects: List<PotionEffect>? = null,
        color: Color? = null,
        basePotionData: PotionData? = null,
    ): ItemStack {
        val item = create(
            Material.SPLASH_POTION, name, localizedName, amount, customModelData, lore, attributeModifier, enchantments, flags, unbreakable
        )
        val meta = item.itemMeta as PotionMeta
        customEffects?.forEach { meta.addCustomEffect(it, true) }
        meta.color = color
        if (basePotionData != null) meta.basePotionData = basePotionData
        item.itemMeta = meta
        return item
    }

    fun createPotion(
        name: String? = null,
        localizedName: String? = null,
        amount: Int? = null,
        customModelData: Int? = null,
        lore: List<String>? = null,
        attributeModifier: Map<Attribute, AttributeModifier>? = null,
        enchantments: Map<Enchantment, Int>? = null,
        flags: List<ItemFlag>? = null,
        unbreakable: Boolean? = null,
        customEffects: List<PotionEffect>? = null,
        color: Color? = null,
        basePotionData: PotionData? = null,
    ): ItemStack {
        val item = create(
            Material.POTION, name, localizedName, amount, customModelData, lore, attributeModifier, enchantments, flags, unbreakable
        )
        val meta = item.itemMeta as PotionMeta
        customEffects?.forEach { meta.addCustomEffect(it, true) }
        meta.color = color
        if (basePotionData != null) meta.basePotionData = basePotionData
        item.itemMeta = meta
        return item
    }

    fun createEnchantBook(
        name: String? = null,
        localizedName: String? = null,
        amount: Int? = null,
        customModelData: Int? = null,
        lore: List<String>? = null,
        attributeModifier: Map<Attribute, AttributeModifier>? = null,
        enchantments: Map<Enchantment, Int>? = null,
        flags: List<ItemFlag>? = null,
        unbreakable: Boolean? = null,
        storedEnchants: Map<Enchantment, Int>? = null,
    ): ItemStack {
        val item = create(
            Material.ENCHANTED_BOOK, name, localizedName, amount, customModelData, lore, attributeModifier, enchantments, flags, unbreakable
        )
        val meta = item.itemMeta as EnchantmentStorageMeta
        storedEnchants?.forEach { meta.addStoredEnchant(it.key, it.value, true) }
        item.itemMeta = meta
        return item
    }

    fun create(
        material: Material,
        name: String? = null,
        localizedName: String? = null,
        amount: Int? = null,
        customModelData: Int? = null,
        lore: List<String>? = null,
        attributeModifier: Map<Attribute, AttributeModifier>? = null,
        enchantments: Map<Enchantment, Int>? = null,
        flags: List<ItemFlag>? = null,
        unbreakable: Boolean? = null,
    ): ItemStack {
        val item = ItemStack(material)
        if (amount != null) item.amount = amount

        val meta = item.itemMeta ?: return item
        if (name != null) meta.setDisplayName(name)
        if (localizedName != null) meta.setLocalizedName(localizedName)
        if (unbreakable != null) meta.isUnbreakable = unbreakable
        if (lore != null) meta.lore = lore
        if (customModelData != null) meta.setCustomModelData(customModelData)

        attributeModifier?.forEach { meta.addAttributeModifier(it.key, it.value) }
        flags?.forEach { meta.addItemFlags(it) }

        item.itemMeta = meta
        enchantments?.forEach { item.addUnsafeEnchantment(it.key, it.value) }
        return item
    }

    fun giveCompassIfNone(player: Player) {
        if (player.inventory.filterNotNull().none { it.type == Material.COMPASS }) player.inventory.addItem(create(Material.COMPASS))
    }
}

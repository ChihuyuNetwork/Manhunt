package love.chihuyu.game

import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.utils.ItemUtil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe

object RecipeManager {

    fun add() {
        plugin.server.addRecipe(
            ShapedRecipe(NamespacedKey.fromString("crimson_boat")!!, ItemUtil.create(Material.OAK_BOAT)).shape(
                "W W", "WWW"
            )
                .setIngredient('W', Material.CRIMSON_PLANKS)
        )

        plugin.server.addRecipe(
            ShapedRecipe(NamespacedKey.fromString("warped_boat")!!, ItemUtil.create(Material.OAK_BOAT)).shape(
                "W W", "WWW"
            )
                .setIngredient('W', Material.WARPED_PLANKS)
        )
    }
}

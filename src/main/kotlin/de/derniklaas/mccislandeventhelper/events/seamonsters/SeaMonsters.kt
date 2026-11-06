package de.derniklaas.mccislandeventhelper.events.seamonsters

import de.derniklaas.mccislandeventhelper.MccIsland
import de.derniklaas.mccislandeventhelper.config.EventHelperConfig
import de.derniklaas.mccislandeventhelper.glyphs.HeartGlyphs
import java.util.Locale
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object SeaMonsters {

    private const val ORDER_ITEM_NAME = "event fish order"

    fun register() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines -> addLocationHearts(stack, lines) }
    }

    private fun addLocationHearts(stack: ItemStack, lines: MutableList<Component>) {
        val config = EventHelperConfig.instance
        if (!config.enabled || !config.seaMonstersLocationHearts) return
        if (!MccIsland.isConnected || !isOrder(stack)) return
        for (index in 1 until lines.size) {
            val line = lines[index]
            val color = FishLocations.heartFor(line.string) ?: continue
            val heart = HeartGlyphs.icon(color) ?: continue
            lines[index] = line.copy().append(" ").append(heart)
        }
    }

    private fun isOrder(stack: ItemStack): Boolean =
        ORDER_ITEM_NAME in stack.hoverName.string.lowercase(Locale.ROOT)
}

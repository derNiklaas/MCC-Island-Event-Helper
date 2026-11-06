package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.ScrapColor
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.EnumMap
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

object ScrapRotation {
    private var games: Map<ScrapColor, List<Identifier>> = emptyMap()
    private var validUntil: Instant = Instant.EPOCH
    private val listeners = mutableListOf<() -> Unit>()

    fun onChange(listener: () -> Unit) {
        listeners += listener
    }

    fun gamesFor(color: ScrapColor): List<Identifier> = games[color].orEmpty()

    fun readNavigator(items: List<ItemStack>, now: Instant) {
        val read = EnumMap<ScrapColor, MutableList<Identifier>>(ScrapColor::class.java)
        for (stack in items) {
            val model = stack.get(DataComponents.ITEM_MODEL) ?: continue
            stack.get(DataComponents.LORE)?.lines().orEmpty()
                .mapNotNull { ScrapParsers.parseRewardLine(it.string) }
                .forEach { color -> read.getOrPut(color) { mutableListOf() } += model }
        }
        if (read.isEmpty()) return
        val until = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
        if (read == games && until == validUntil) return
        games = read
        validUntil = until
        listeners.forEach { it() }
    }

    fun expire(now: Instant) {
        if (games.isEmpty() || now.isBefore(validUntil)) return
        games = emptyMap()
        listeners.forEach { it() }
    }
}

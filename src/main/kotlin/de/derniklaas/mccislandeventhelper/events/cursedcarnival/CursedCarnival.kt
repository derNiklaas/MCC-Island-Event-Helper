package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import de.derniklaas.mccislandeventhelper.MccIsland
import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import de.derniklaas.mccislandeventhelper.config.EventHelperConfig
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.ScrapColor
import de.derniklaas.mccislandeventhelper.hud.EventHelperDialog
import de.derniklaas.mccislandeventhelper.hud.HudWidget
import de.derniklaas.mccislandeventhelper.hud.HudWidgets
import java.time.Instant
import java.util.EnumMap
import kotlin.jvm.optionals.getOrNull
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import org.slf4j.LoggerFactory

object CursedCarnival {

    val ENDS_AT: Instant = Instant.parse("2026-11-09T14:00:00Z")

    private val logger = LoggerFactory.getLogger(MOD_ID)

    private var apiRequested = false

    fun isRunning(now: Instant = Instant.now()): Boolean = now.isBefore(ENDS_AT)

    private val countdown = object : HudWidget() {
        override fun shouldShow() =
            EventHelperConfig.instance.cursedCarnivalCountdown && CarnivalSchedule.next(Instant.now()) != null

        override fun showInGames() = EventHelperConfig.instance.cursedCarnivalCountdownInGames

        override fun create(x: Int, y: Int): EventHelperDialog = CountdownDialog(x, y)

        override var x: Int
            get() = EventHelperConfig.instance.cursedCarnivalCountdownX
            set(value) { EventHelperConfig.instance.cursedCarnivalCountdownX = value }

        override var y: Int
            get() = EventHelperConfig.instance.cursedCarnivalCountdownY
            set(value) { EventHelperConfig.instance.cursedCarnivalCountdownY = value }
    }

    private val scraps = object : HudWidget() {
        override fun shouldShow() = EventHelperConfig.instance.cursedCarnivalScraps && isRunning()

        override fun create(x: Int, y: Int): EventHelperDialog = ScrapDialog(x, y)

        override var x: Int
            get() = EventHelperConfig.instance.cursedCarnivalScrapsX
            set(value) { EventHelperConfig.instance.cursedCarnivalScrapsX = value }

        override var y: Int
            get() = EventHelperConfig.instance.cursedCarnivalScrapsY
            set(value) { EventHelperConfig.instance.cursedCarnivalScrapsY = value }
    }

    fun register() {
        HudWidgets.add(countdown)
        HudWidgets.add(scraps)
        ScrapTracker.onChange(scraps::refresh)
        ScrapRotation.onChange(scraps::refresh)
        ClientTickEvents.END_CLIENT_TICK.register { ScrapRotation.expire(Instant.now()) }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if (!MccIsland.isConnected || !isRunning()) return@register
            ScrapTracker.startSession(client.user.profileId)
            requestApiCounts(client)
        }
        EventHelperConfig.onSave {
            scraps.refresh()
            if (isTracking()) requestApiCounts(Minecraft.getInstance())
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            apiRequested = false
            ScrapTracker.endSession()
        }

        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            if (overlay || !isTracking()) return@register
            val gain = ScrapParsers.parseGain(message.string) ?: return@register
            ScrapTracker.add(gain.color, gain.amount)
        }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (!isTracking() || screen !is AbstractContainerScreen<*>) return@register
            // Titles start with private-use glyphs that draw the menu background.
            val title = screen.title.string.filterNot { it.isSurrogate() || Character.getType(it) == Character.PRIVATE_USE.toInt() }
            if (title.startsWith("NAVIGATOR")) {
                ScreenEvents.afterTick(screen).register { ScrapRotation.readNavigator(menuItems(screen), Instant.now()) }
                return@register
            }
            val infinibag = title.startsWith("INFINIBAG")
            ScreenEvents.afterTick(screen).register { readMenu(screen, infinibag) }
        }
    }

    private fun menuItems(screen: AbstractContainerScreen<*>): List<ItemStack> =
        screen.menu.slots.filter { it.container !is Inventory }.map { it.item }

    private fun isTracking() = MccIsland.isConnected && isRunning()

    private fun readMenu(screen: AbstractContainerScreen<*>, infinibag: Boolean) {
        val bagged = EnumMap(ScrapColor.entries.associateWith { 0 })
        var sawScrap = false
        for (stack in menuItems(screen)) {
            val model = stack.get(DataComponents.ITEM_MODEL) ?: continue
            if (model == ScrapColor.TICKET_MODEL) {
                val owned = stack.get(DataComponents.LORE)?.lines().orEmpty()
                    .mapNotNull { ScrapParsers.parseCostLine(it.string) }
                    .toMap()
                if (owned.size == ScrapColor.entries.size) ScrapTracker.setFromMenu(owned)
            } else if (infinibag) {
                val color = ScrapColor.byModel(model) ?: continue
                val amount = infinibagAmount(stack) ?: continue
                bagged.merge(color, amount, Int::plus)
                sawScrap = true
            }
        }
        if (sawScrap) ScrapTracker.setFromMenu(bagged)
    }

    private fun infinibagAmount(stack: ItemStack): Int? =
        stack.get(DataComponents.CUSTOM_DATA)?.copyTag()
            ?.getCompound("PublicBukkitValues")?.getOrNull()
            ?.getInt("mcc:amount")?.getOrNull()

    private fun requestApiCounts(client: Minecraft) {
        if (apiRequested || !EventHelperConfig.instance.cursedCarnivalStatsApi) return
        apiRequested = true
        StatsApi.fetchScraps(client.user.name).whenComplete { result, error ->
            client.execute {
                when {
                    error != null -> logger.warn("Couldn't load ticket scrap from stats.derniklaas.de", error)
                    result is StatsApi.Result.Counts -> ScrapTracker.setFromApi(result.counts)
                    else -> logger.info("Ticket scrap not available from the API; the Infinibag API setting is off")
                }
            }
        }
    }
}

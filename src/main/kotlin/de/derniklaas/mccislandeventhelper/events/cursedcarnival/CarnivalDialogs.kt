package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import com.noxcrew.sheeplib.layout.LinearLayout
import com.noxcrew.sheeplib.layout.grid
import com.noxcrew.sheeplib.layout.linear
import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import de.derniklaas.mccislandeventhelper.config.EventHelperConfig
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.CarnivalActivity
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.ScrapColor
import de.derniklaas.mccislandeventhelper.glyphs.IconGlyphs
import de.derniklaas.mccislandeventhelper.hud.EventHelperDialog
import de.derniklaas.mccislandeventhelper.hud.ItemIconWidget
import de.derniklaas.mccislandeventhelper.hud.LiveTextWidget
import java.time.Instant
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.objects.AtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

private const val KEY = "hud.$MOD_ID.cursed_carnival"

private fun titleWithIcon(icon: Component?, text: Component): Component =
    if (icon == null) text else Component.empty().append(icon).append(" ").append(text)

private fun cursedCarnivalIcon(): Component? = IconGlyphs.icon("cursed_carnival_event")

private fun ticketIcon(): Component = Component.`object`(
    AtlasSprite(AtlasSprite.DEFAULT_ATLAS, ScrapColor.TICKET_MODEL),
)

class CountdownDialog(x: Int, y: Int) : EventHelperDialog(
    x,
    y,
    titleWithIcon(cursedCarnivalIcon(), Component.translatable("$KEY.countdown.title")),
) {

    override fun content() = linear(LinearLayout.Orientation.VERTICAL) {
        val font = Minecraft.getInstance().font
        val widest = CarnivalActivity.entries.maxOf { font.width(line(it, "15:00", soon = false)) }
        +LiveTextWidget(widest) {
            val now = Instant.now()
            val next = CarnivalSchedule.next(now) ?: return@LiveTextWidget Component.empty()
            val remaining = next.startsAt.toEpochMilli() - now.toEpochMilli()
            line(next.activity, CarnivalSchedule.formatRemaining(now, next.startsAt), soon = remaining <= SOON_MILLIS)
        }
    }

    private fun line(activity: CarnivalActivity, time: String, soon: Boolean): Component =
        Component.translatable(
            "$KEY.countdown.next",
            Component.literal(activity.displayName).withStyle(ChatFormatting.WHITE),
            Component.literal(time).withStyle(if (soon) ChatFormatting.YELLOW else ChatFormatting.WHITE),
        ).withStyle(ChatFormatting.GRAY)

    private companion object {
        const val SOON_MILLIS = 60_000L
    }
}

class ScrapDialog(x: Int, y: Int) : EventHelperDialog(
    x,
    y,
    titleWithIcon(ticketIcon(), Component.translatable("$KEY.scraps.title")),
) {

    override fun content() = linear(LinearLayout.Orientation.VERTICAL) {
        val font = Minecraft.getInstance().font
        val counts = ScrapTracker.snapshot
        val missing = TicketMath.missingForNext(counts)
        val showGames = EventHelperConfig.instance.cursedCarnivalScrapGames

        +grid {
            ScrapColor.entries.forEachIndexed { column, color ->
                ItemIconWidget(icons.getValue(color)).at(0, column)
                StringWidget(countText(counts.getValue(color), color in missing), font).at(1, column)
                if (showGames && color in missing) {
                    ScrapRotation.gamesFor(color).chunked(2).forEachIndexed { row, pair ->
                        gameIcons(pair).at(2 + row, column)
                    }
                }
            }
        }

        val craftable = TicketMath.craftable(counts)
        if (craftable > 0) {
            val key = if (craftable == 1) "$KEY.scraps.craftable.one" else "$KEY.scraps.craftable.many"
            +StringWidget(Component.translatable(key, craftable).withStyle(ChatFormatting.GREEN), font)
        }
        val needs = missing.entries.joinToString(", ") { (color, amount) -> "$amount ${color.displayName}" }
        +StringWidget(Component.translatable("$KEY.scraps.missing", needs).withStyle(ChatFormatting.GRAY), font)
    }

    private fun gameIcons(models: List<Identifier>): LayoutElement {
        if (models.size == 1) return ItemIconWidget(iconStack(models.single()), size = GAME_ICON_SIZE, width = 16)
        return LinearLayout(LinearLayout.Orientation.HORIZONTAL, 0) {
            models.forEach { +ItemIconWidget(iconStack(it), size = GAME_ICON_SIZE) }
        }
    }

    private fun countText(count: Int, short: Boolean): Component =
        Component.literal(count.toString()).withStyle(if (short) ChatFormatting.RED else ChatFormatting.WHITE)

    private companion object {
        const val GAME_ICON_SIZE = 8

        fun iconStack(model: Identifier) = ItemStack(Items.ECHO_SHARD).apply { set(DataComponents.ITEM_MODEL, model) }

        val icons = ScrapColor.entries.associateWith { iconStack(it.itemModel) }
    }
}

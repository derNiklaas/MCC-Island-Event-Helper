package de.derniklaas.mccislandeventhelper.hud

import com.noxcrew.sheeplib.dialog.Dialog
import com.noxcrew.sheeplib.dialog.title.DialogTitleWidget
import com.noxcrew.sheeplib.dialog.title.TextTitleWidget
import com.noxcrew.sheeplib.theme.DefaultTheme
import com.noxcrew.sheeplib.theme.Theme
import com.noxcrew.sheeplib.theme.Themed
import com.noxcrew.sheeplib.util.opacity
import de.derniklaas.mccislandeventhelper.glyphs.IconGlyphs
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object EventHelperTheme : Theme by DefaultTheme {
    override val theme: Theme = this
    override val colors = object : Theme.Colors by DefaultTheme.colors {
        override val dialogBackgroundAlt = 0x000000 opacity 160
    }
}

abstract class EventHelperDialog(x: Int, y: Int, private val titleText: Component) :
    Dialog(x, y), Themed by EventHelperTheme {

    final override val title: DialogTitleWidget = TextTitleWidget(this, titleText, isCloseable = false)

    private val glyphGeneration = IconGlyphs.generation

    private val titleWidth = Minecraft.getInstance().font.width(titleText)

    internal fun isStale(): Boolean =
        glyphGeneration != IconGlyphs.generation || titleWidth != Minecraft.getInstance().font.width(titleText)

    protected abstract fun content(): Layout

    final override fun layout(): Layout {
        val frame = FrameLayout(titleWidth, 0)
        frame.addChild(content(), frame.newChildLayoutSettings().alignHorizontallyLeft().alignVerticallyTop())
        return frame
    }

    fun refresh() {
        if (!state.isClosing) init()
    }
}

class LiveTextWidget(width: Int, private val text: () -> Component) :
    AbstractWidget(0, 0, width, Minecraft.getInstance().font.lineHeight, Component.empty()) {

    init {
        active = false
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        graphics.text(Minecraft.getInstance().font, text(), x, y, -1)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) = Unit
}

/** Draws the item at [size] px, centered horizontally in [width]. Halving keeps the pixels crisp. */
class ItemIconWidget(
    private val stack: ItemStack,
    private val size: Int = ITEM_SIZE,
    width: Int = size,
) : AbstractWidget(0, 0, width, size, Component.empty()) {

    init {
        active = false
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val left = x + (width - size) / 2
        if (size == ITEM_SIZE) {
            graphics.fakeItem(stack, left, y)
            return
        }
        val pose = graphics.pose()
        pose.pushMatrix()
        pose.translate(left.toFloat(), y.toFloat())
        pose.scale(size / ITEM_SIZE.toFloat())
        graphics.fakeItem(stack, 0, 0)
        pose.popMatrix()
    }

    private companion object {
        const val ITEM_SIZE = 16
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) = Unit
}

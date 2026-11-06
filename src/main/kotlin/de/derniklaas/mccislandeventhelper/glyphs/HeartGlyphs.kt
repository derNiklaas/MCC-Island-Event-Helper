package de.derniklaas.mccislandeventhelper.glyphs

import java.util.concurrent.ConcurrentHashMap
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier

enum class HeartColor(val glyphName: String) {
    RED("heart"),
    BLACK("heart_black"),
    BLUE("heart_blue"),
    GREEN("heart_green"),
    ORANGE("heart_orange"),
    PINK("heart_pink"),
    PURPLE("heart_purple"),
    WHITE("heart_white"),
    YELLOW("heart_yellow"),
}

object HeartGlyphs {

    private const val MCC_NAMESPACE = "mcc"

    private const val GLYPH_DIRECTORY = "_fonts/icon/emojis/"

    private val ICON_FONT = FontDescription.Resource(Identifier.fromNamespaceAndPath(MCC_NAMESPACE, "icon"))

    private val HEART_STYLE: Style = Style.EMPTY
        .withFont(ICON_FONT)
        .withColor(ChatFormatting.WHITE)
        .withItalic(false)

    private val COLORS_BY_GLYPH_NAME = HeartColor.entries.associateBy(HeartColor::glyphName)

    private val codepoints = ConcurrentHashMap<HeartColor, Int>()

    fun register(file: Identifier, codepointGrid: Array<IntArray>) {
        if (file.namespace != MCC_NAMESPACE || !file.path.startsWith(GLYPH_DIRECTORY)) return
        val glyphName = file.path.removePrefix(GLYPH_DIRECTORY).removeSuffix(".png")
        val color = COLORS_BY_GLYPH_NAME[glyphName] ?: return
        val codepoint = codepointGrid.firstNotNullOfOrNull { row -> row.firstOrNull { it != 0 } } ?: return
        codepoints[color] = codepoint
    }

    fun icon(color: HeartColor): Component? {
        val codepoint = codepoints[color] ?: return null
        return Component.literal(String(Character.toChars(codepoint))).withStyle(HEART_STYLE)
    }
}

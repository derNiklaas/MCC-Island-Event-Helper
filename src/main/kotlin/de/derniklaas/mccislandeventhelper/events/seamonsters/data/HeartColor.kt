package de.derniklaas.mccislandeventhelper.events.seamonsters.data

import de.derniklaas.mccislandeventhelper.glyphs.IconGlyphs
import net.minecraft.network.chat.Component

enum class HeartColor(val glyphName: String) {
    RED("heart"),
    BLUE("heart_blue"),
    GREEN("heart_green"),
    ORANGE("heart_orange"),
    PINK("heart_pink"),
    PURPLE("heart_purple"),
    WHITE("heart_white"),
    YELLOW("heart_yellow"),
    ;

    val icon: Component?
        get() = IconGlyphs.icon("emojis/$glyphName")
}

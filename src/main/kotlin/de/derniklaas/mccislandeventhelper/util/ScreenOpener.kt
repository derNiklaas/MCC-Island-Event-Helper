package de.derniklaas.mccislandeventhelper.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.screens.Screen

object ScreenOpener {
    private val SET_SCREEN_TYPE: MethodType = MethodType.methodType(Void.TYPE, Screen::class.java)

    private val onMinecraft: MethodHandle? = find(Minecraft::class.java)

    private val onGui: MethodHandle? = if (onMinecraft == null) find(Gui::class.java) else null

    init {
        check(onMinecraft != null || onGui != null) { "Found neither Minecraft.setScreen nor Gui.setScreen" }
    }

    fun open(screen: Screen?) {
        val client = Minecraft.getInstance()
        if (onMinecraft != null) {
            onMinecraft.invokeWithArguments(client, screen)
        } else {
            onGui!!.invokeWithArguments(client.gui, screen)
        }
    }

    private fun find(owner: Class<*>): MethodHandle? = try {
        MethodHandles.publicLookup().findVirtual(owner, "setScreen", SET_SCREEN_TYPE)
    } catch (_: NoSuchMethodException) {
        null
    }
}

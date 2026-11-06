package de.derniklaas.mccislandeventhelper.hud

import com.noxcrew.sheeplib.DialogContainer
import de.derniklaas.mccislandeventhelper.MccIsland
import de.derniklaas.mccislandeventhelper.MccLocation
import de.derniklaas.mccislandeventhelper.config.EventHelperConfig
import de.derniklaas.mccislandeventhelper.glyphs.IconGlyphs
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.screens.ChatScreen

abstract class HudWidget {

    var dialog: EventHelperDialog? = null
        internal set

    abstract fun shouldShow(): Boolean

    open fun showInGames(): Boolean = false

    abstract fun create(x: Int, y: Int): EventHelperDialog

    abstract var x: Int
    abstract var y: Int

    fun refresh() {
        dialog?.refresh()
    }
}

object HudWidgets {
    private const val CHECK_INTERVAL_TICKS = 10

    private val widgets = mutableListOf<HudWidget>()
    private var ticks = 0

    fun add(widget: HudWidget) {
        widgets += widget
    }

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (++ticks % CHECK_INTERVAL_TICKS == 0) update()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> widgets.forEach(::close) }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (screen is ChatScreen) ScreenEvents.remove(screen).register { savePositions() }
        }
    }

    private fun update() {
        val config = EventHelperConfig.instance
        val allowed = config.enabled && MccIsland.isConnected && IconGlyphs.isMccPackLoaded
        val inGame = MccLocation.isInGame
        for (widget in widgets) {
            val show = allowed && (!inGame || widget.showInGames()) && widget.shouldShow()
            val dialog = widget.dialog
            when {
                show && dialog == null -> open(widget)
                !show && dialog != null -> close(widget)
                dialog != null && dialog.isStale() -> {
                    close(widget)
                    open(widget)
                }
            }
        }
    }

    private fun open(widget: HudWidget) {
        val dialog = widget.create(widget.x, widget.y)
        widget.dialog = dialog
        DialogContainer += dialog
    }

    private fun close(widget: HudWidget) {
        val dialog = widget.dialog ?: return
        widget.x = dialog.x
        widget.y = dialog.y
        widget.dialog = null
        dialog.close()
    }

    private fun savePositions() {
        var moved = false
        for (widget in widgets) {
            val dialog = widget.dialog ?: continue
            if (dialog.x != widget.x || dialog.y != widget.y) {
                widget.x = dialog.x
                widget.y = dialog.y
                moved = true
            }
        }
        if (moved) EventHelperConfig.save()
    }
}

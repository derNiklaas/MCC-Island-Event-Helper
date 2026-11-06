package de.derniklaas.mccislandeventhelper

import de.derniklaas.mccislandeventhelper.config.ConfigCommand
import de.derniklaas.mccislandeventhelper.config.EventHelperConfig
import de.derniklaas.mccislandeventhelper.debug.CaptureLogger
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.CursedCarnival
import de.derniklaas.mccislandeventhelper.events.seamonsters.SeaMonsters
import de.derniklaas.mccislandeventhelper.glyphs.IconGlyphs
import de.derniklaas.mccislandeventhelper.hud.HudWidgets
import net.fabricmc.api.ClientModInitializer

class MccIslandEventHelperClient : ClientModInitializer {

    companion object {
        const val MOD_ID = "mccislandeventhelper"
    }

    override fun onInitializeClient() {
        EventHelperConfig.load()
        MccIsland.register()
        MccLocation.register()
        IconGlyphs.register()
        ConfigCommand.register()
        CaptureLogger.register()

        SeaMonsters.register()
        CursedCarnival.register()

        HudWidgets.register()
    }
}

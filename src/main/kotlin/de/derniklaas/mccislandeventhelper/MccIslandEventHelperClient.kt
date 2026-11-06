package de.derniklaas.mccislandeventhelper

import de.derniklaas.mccislandeventhelper.config.ConfigCommand
import de.derniklaas.mccislandeventhelper.config.EventHelperConfig
import de.derniklaas.mccislandeventhelper.events.seamonsters.SeaMonsters
import net.fabricmc.api.ClientModInitializer

class MccIslandEventHelperClient : ClientModInitializer {

    companion object {
        const val MOD_ID = "mccislandeventhelper"
    }

    override fun onInitializeClient() {
        EventHelperConfig.load()
        MccIsland.register()
        ConfigCommand.register()

        SeaMonsters.register()
    }
}

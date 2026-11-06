package de.derniklaas.mccislandeventhelper.config

import com.mojang.brigadier.Command
import de.derniklaas.mccislandeventhelper.util.ScreenOpener
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object ConfigCommand {

    private const val NAME = "eventhelper"

    private var openOnNextTick = false

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal(NAME).executes {
                    openOnNextTick = true
                    Command.SINGLE_SUCCESS
                }
            )
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            if (openOnNextTick) {
                openOnNextTick = false
                ScreenOpener.open(ConfigScreen.create(null))
            }
        }
    }
}

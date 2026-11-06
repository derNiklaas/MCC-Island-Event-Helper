package de.derniklaas.mccislandeventhelper.config

import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.dsl.OptionRegistrar
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.binding
import dev.isxander.yacl3.dsl.controller
import dev.isxander.yacl3.dsl.tickBox
import kotlin.reflect.KMutableProperty0
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object ConfigScreen {

    fun create(parent: Screen?): Screen {
        val config = EventHelperConfig.instance
        val defaults = EventHelperConfig.defaults

        return YetAnotherConfigLib(MOD_ID) {
            title(Component.translatable("config.$MOD_ID.title"))
            save { EventHelperConfig.save() }

            categories.register("general") {
                name(Component.translatable("config.$MOD_ID.general"))
                rootOptions.toggle("general", "enabled", config::enabled, defaults.enabled)
            }

            categories.register("sea_monsters") {
                name(Component.translatable("config.$MOD_ID.sea_monsters"))
                rootOptions.toggle(
                    "sea_monsters",
                    "location_hearts",
                    config::seaMonstersLocationHearts,
                    defaults.seaMonstersLocationHearts,
                )
            }
        }.generateScreen(parent)
    }

private fun OptionRegistrar.toggle(
        category: String,
        option: String,
        property: KMutableProperty0<Boolean>,
        default: Boolean,
    ) {
        val key = "config.$MOD_ID.$category.$option"
        register(option) {
            name(Component.translatable(key))
            description(OptionDescription.of(Component.translatable("$key.description")))
            binding(property, default)
            controller = tickBox()
        }
    }
}

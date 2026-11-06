package de.derniklaas.mccislandeventhelper.config

import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier

class EventHelperConfig {

    @JvmField
    @SerialEntry(comment = "Enable or Disable the mod and all of its features, regardless of other settings.")
    var enabled: Boolean = true

    @JvmField
    @SerialEntry(comment = "Sea Monsters: Show colored hearts behind the name of fish in the order menu.")
    var seaMonstersLocationHearts: Boolean = true

    companion object {

        val HANDLER: ConfigClassHandler<EventHelperConfig> =
            ConfigClassHandler.createBuilder(EventHelperConfig::class.java)
                .id(Identifier.fromNamespaceAndPath(MOD_ID, "config"))
                .serializer { handler ->
                    GsonConfigSerializerBuilder.create(handler)
                        .setPath(FabricLoader.getInstance().configDir.resolve("$MOD_ID.json5"))
                        .setJson5(true)
                        .build()
                }
                .build()

        val instance: EventHelperConfig
            get() = HANDLER.instance()

        val defaults: EventHelperConfig
            get() = HANDLER.defaults()

        fun load() {
            HANDLER.load()
        }

        fun save() {
            HANDLER.save()
        }
    }
}

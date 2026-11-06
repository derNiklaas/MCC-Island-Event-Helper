package de.derniklaas.mccislandeventhelper.config

import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier

class EventHelperConfig {

    @JvmField
    @SerialEntry
    var enabled: Boolean = true

    @JvmField
    @SerialEntry
    var seaMonstersLocationHearts: Boolean = true

    @JvmField
    @SerialEntry
    var cursedCarnivalCountdown: Boolean = true

    @JvmField
    @SerialEntry
    var cursedCarnivalCountdownInGames: Boolean = false

    @JvmField
    @SerialEntry
    var cursedCarnivalScraps: Boolean = true

    @JvmField
    @SerialEntry
    var cursedCarnivalScrapGames: Boolean = true

    @JvmField
    @SerialEntry
    var cursedCarnivalStatsApi: Boolean = false

    @JvmField
    @SerialEntry
    var cursedCarnivalCountdownX: Int = 5

    @JvmField
    @SerialEntry
    var cursedCarnivalCountdownY: Int = 5

    @JvmField
    @SerialEntry
    var cursedCarnivalScrapsX: Int = 5

    @JvmField
    @SerialEntry
    var cursedCarnivalScrapsY: Int = 60

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

        private val saveListeners = mutableListOf<() -> Unit>()

        fun onSave(listener: () -> Unit) {
            saveListeners += listener
        }

        fun save() {
            HANDLER.save()
            saveListeners.forEach { it() }
        }
    }
}

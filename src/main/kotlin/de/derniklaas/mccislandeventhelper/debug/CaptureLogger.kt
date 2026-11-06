package de.derniklaas.mccislandeventhelper.debug

import com.mojang.serialization.JsonOps
import de.derniklaas.mccislandeventhelper.MccIsland
import de.derniklaas.mccislandeventhelper.MccLocation
import java.io.BufferedWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.Locale
import kotlin.jvm.optionals.getOrNull
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

/** Dev util to help create modules for events */
object CaptureLogger {

    private const val FILE_NAME = "eventhelper-capture.log"

    // Menus can animate their items.
    private const val MAX_DUMPS_PER_SCREEN = 20

    private val DETAILED_KEYWORDS = listOf("scrap", "ticket", "carnival")

    private val enabled = FabricLoader.getInstance().isDevelopmentEnvironment &&
            System.getProperty("eventhelper.capture") != "false"

    private val writer: BufferedWriter by lazy {
        val path = FabricLoader.getInstance().gameDir.resolve(FILE_NAME)
        Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    private var lastOverlay: String? = null

    fun register() {
        if (!enabled) return

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            log("JOIN", "server=${client.currentServer?.ip} mccIsland=${MccIsland.isConnected}")
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> log("DISCONNECT", "") }

        MccLocation.onChange { server, types -> log("LOCATION", "server=$server types=$types") }

        ClientReceiveMessageEvents.GAME.register { message, overlay -> logMessage(message, overlay) }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (!MccIsland.isConnected || screen !is AbstractContainerScreen<*>) return@register
            log("SCREEN", "title=\"${screen.title.string}\" class=${screen.javaClass.simpleName}")
            log("SCREEN_JSON", json(screen.title))

            // Items arrive after the screen opens and can change while it's open.
            var lastSignature = ""
            var dumps = 0
            ScreenEvents.afterTick(screen).register {
                if (dumps >= MAX_DUMPS_PER_SCREEN) return@register
                val slots = screen.menu.slots.withIndex()
                    .filter { (_, slot) -> slot.container !is Inventory && !slot.item.isEmpty }
                val signature =
                    slots.joinToString("|") { (index, slot) -> "$index:${slot.item.hoverName.string}:${slot.item.count}" }
                if (signature == lastSignature) return@register
                lastSignature = signature
                dumps++
                log("SLOTS", "title=\"${screen.title.string}\" dump=$dumps filled=${slots.size}")
                slots.forEach { (index, slot) -> logItem(index, slot.item) }
            }
        }
    }

    private fun logMessage(message: Component, overlay: Boolean) {
        if (!MccIsland.isConnected) return
        val text = message.string
        if (overlay) {
            // MCC Island resends the action bar constantly.
            if (text == lastOverlay) return
            lastOverlay = text
            log("ACTIONBAR", text)
            return
        }
        log("CHAT", text)
        log("CHAT_JSON", json(message))
    }

    private fun logItem(index: Int, stack: ItemStack) {
        val name = stack.hoverName.string
        val model = stack.get(DataComponents.ITEM_MODEL)
        val lore = stack.get(DataComponents.LORE)?.lines().orEmpty()
        log(
            "  SLOT $index",
            "name=\"$name\" count=${stack.count} item=${BuiltInRegistries.ITEM.getKey(stack.item)} model=$model"
        )
        lore.forEach { log("    LORE", it.string) }

        val lowerName = name.lowercase(Locale.ROOT)
        if (DETAILED_KEYWORDS.any { it in lowerName }) {
            log("    NAME_JSON", json(stack.hoverName))
            lore.forEach { log("    LORE_JSON", json(it)) }
            val amount = stack.get(DataComponents.CUSTOM_DATA)?.copyTag()
                ?.getCompound("PublicBukkitValues")?.getOrNull()
                ?.getInt("mcc:amount")?.getOrNull()
            log("    CUSTOM_DATA", "mcc:amount=${amount ?: "<none>"}")
        }
    }

    private fun json(component: Component): String = try {
        val ops = Minecraft.getInstance().level?.registryAccess()?.createSerializationContext(JsonOps.INSTANCE)
            ?: JsonOps.INSTANCE
        ComponentSerialization.CODEC.encodeStart(ops, component).getOrThrow().toString()
    } catch (exception: Exception) {
        "<could not serialize: ${exception.message}>"
    }

    @Synchronized
    private fun log(tag: String, text: String) {
        writer.write("${Instant.now()} [$tag] $text")
        writer.newLine()
        writer.flush()
    }
}

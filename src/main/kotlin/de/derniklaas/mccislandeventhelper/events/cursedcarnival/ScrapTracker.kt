package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.ScrapColor
import java.nio.file.Files
import java.util.EnumMap
import java.util.EnumSet
import java.util.UUID
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

object ScrapTracker {
    private val logger = LoggerFactory.getLogger(MOD_ID)
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = FabricLoader.getInstance().configDir.resolve("$MOD_ID-cursed-carnival.json")

    private val counts = EnumMap(ScrapColor.entries.associateWith { 0 })
    private var playerId: UUID? = null
    private val listeners = mutableListOf<() -> Unit>()

    private val liveColors = EnumSet.noneOf(ScrapColor::class.java)

    val snapshot: Map<ScrapColor, Int>
        get() = counts.toMap()

    fun onChange(listener: () -> Unit) {
        listeners += listener
    }

    fun startSession(player: UUID) {
        if (playerId == player) return
        playerId = player
        counts.putAll(readSaved(player))
        listeners.forEach { it() }
    }

    fun endSession() {
        liveColors.clear()
    }

    fun setFromMenu(values: Map<ScrapColor, Int>) {
        liveColors += values.keys
        update { counts.putAll(values) }
    }

    fun setFromApi(values: Map<ScrapColor, Int>) {
        update { counts.putAll(values.filterKeys { it !in liveColors }) }
    }

    fun add(color: ScrapColor, amount: Int) {
        liveColors += color
        update { counts[color] = counts.getValue(color) + amount }
    }

    private fun update(change: () -> Unit) {
        val before = counts.toMap()
        change()
        if (before == counts) return
        save()
        listeners.forEach { it() }
    }

    private fun readSaved(player: UUID): Map<ScrapColor, Int> {
        val saved = try {
            readFile().getAsJsonObject(player.toString()) ?: JsonObject()
        } catch (exception: Exception) {
            logger.warn("Couldn't read saved ticket scrap counts", exception)
            JsonObject()
        }
        return ScrapColor.entries.associateWith { saved.get(it.name)?.asInt ?: 0 }
    }

    private fun save() {
        val player = playerId ?: return
        try {
            val root = readFile()
            root.add(player.toString(), JsonObject().apply { counts.forEach { (color, owned) -> addProperty(color.name, owned) } })
            Files.writeString(file, gson.toJson(root))
        } catch (exception: Exception) {
            logger.warn("Couldn't save ticket scrap counts", exception)
        }
    }

    private fun readFile(): JsonObject =
        if (Files.isRegularFile(file)) JsonParser.parseString(Files.readString(file)).asJsonObject else JsonObject()
}

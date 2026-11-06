package de.derniklaas.mccislandeventhelper.glyphs

import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import java.util.concurrent.ConcurrentHashMap
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

object IconGlyphs {

    private const val MCC_NAMESPACE = "mcc"
    private const val GLYPH_DIRECTORY = "_fonts/icon/"

    private val STYLE: Style = Style.EMPTY
        .withFont(FontDescription.Resource(Identifier.fromNamespaceAndPath(MCC_NAMESPACE, "icon")))
        .withColor(ChatFormatting.WHITE)
        .withItalic(false)

    private val pending = ConcurrentHashMap<String, Int>()

    @Volatile
    private var published: Map<String, Int> = emptyMap()

    @Volatile
    var generation: Int = 0
        private set

    val isMccPackLoaded: Boolean
        get() = published.isNotEmpty()

    fun register() {
        val id = Identifier.fromNamespaceAndPath(MOD_ID, "icon_glyphs")
        val loader = ResourceLoader.get(PackType.CLIENT_RESOURCES)
        loader.registerReloadListener(id, ResourceManagerReloadListener { onFontsApplied() })
        loader.addListenerOrdering(ResourceReloaderKeys.Client.FONTS, id)
    }

    fun capture(file: Identifier, codepointGrid: Array<IntArray>) {
        if (file.namespace != MCC_NAMESPACE || !file.path.startsWith(GLYPH_DIRECTORY)) return
        val name = file.path.removePrefix(GLYPH_DIRECTORY).removeSuffix(".png")
        val codepoint = codepointGrid.firstNotNullOfOrNull { row -> row.firstOrNull { it != 0 } } ?: return
        pending[name] = codepoint
    }

    internal fun onFontsApplied() {
        published = pending.toMap()
        pending.clear()
        generation++
    }

    fun icon(name: String): Component? {
        val codepoint = published[name] ?: return null
        return Component.literal(String(Character.toChars(codepoint))).withStyle(STYLE)
    }
}

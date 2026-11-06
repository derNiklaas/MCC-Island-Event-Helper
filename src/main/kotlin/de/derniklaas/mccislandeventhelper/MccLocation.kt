package de.derniklaas.mccislandeventhelper

import com.noxcrew.noxesium.core.mcc.ClientboundMccServerPacket
import com.noxcrew.noxesium.core.mcc.MccPackets
import java.util.concurrent.CopyOnWriteArrayList
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

object MccLocation {

    private const val GAME_TYPE = "game"

    @Volatile
    var server: String? = null
        private set

    @Volatile
    var types: List<String> = emptyList()
        private set

    val isInGame: Boolean
        get() = GAME_TYPE in types

    private val listeners = CopyOnWriteArrayList<(server: String, types: List<String>) -> Unit>()

    fun onChange(listener: (server: String, types: List<String>) -> Unit) {
        listeners += listener
    }

    fun register() {
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            server = null
            types = emptyList()
        }
    }

    internal fun registerNoxesiumListener() {
        MccPackets.CLIENTBOUND_MCC_SERVER.addListener(this, ClientboundMccServerPacket::class.java) { _, packet, _ ->
            server = packet.server()
            types = packet.types().toList()
            listeners.forEach { it(packet.server(), types) }
        }
    }
}

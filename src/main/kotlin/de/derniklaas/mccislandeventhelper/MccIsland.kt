package de.derniklaas.mccislandeventhelper

import java.util.Locale
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

object MccIsland {

    private const val DOMAIN = "mccisland.net"

    @Volatile
    var isConnected: Boolean = false
        private set

    fun register() {
        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            isConnected = client.currentServer?.ip?.let(::isMccIslandAddress) ?: false
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> isConnected = false }
    }

    /** Accepts the bare domain and any subdomain, with or without a port. */
    internal fun isMccIslandAddress(address: String): Boolean {
        val host = address.lowercase(Locale.ROOT).substringBeforeLast(':')
        return host == DOMAIN || host.endsWith(".$DOMAIN")
    }
}

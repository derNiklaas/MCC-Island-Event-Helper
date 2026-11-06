package de.derniklaas.mccislandeventhelper

import com.noxcrew.noxesium.core.fabric.mcc.MccNoxesiumEntrypoint

/** Entrypoint for the Noxesium Packet Listener. */
class EventHelperNoxesiumEntrypoint : MccNoxesiumEntrypoint() {

    override fun initialize() {
        MccLocation.registerNoxesiumListener()
    }
}

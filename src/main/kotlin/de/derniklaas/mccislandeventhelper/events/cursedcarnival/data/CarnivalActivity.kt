package de.derniklaas.mccislandeventhelper.events.cursedcarnival.data

import java.time.Instant

/** Each activity starts at [minute] past every hour, UTC. */
enum class CarnivalActivity(val displayName: String, val minute: Int) {
    CRYPT_COLLECTORS("Crypt Collectors", 0),
    TRICKY_TOWER("Tricky Tower", 15),
    CREEPY_CRITTERS("Creepy Critters", 30),
    SCARECROW_SPRINT("Scarecrow Sprint", 45),
    ;

    companion object {
        private val BY_MINUTE = entries.associateBy(CarnivalActivity::minute)

        fun at(minute: Int): CarnivalActivity? = BY_MINUTE[minute]
    }
}

data class UpcomingActivity(val activity: CarnivalActivity, val startsAt: Instant)

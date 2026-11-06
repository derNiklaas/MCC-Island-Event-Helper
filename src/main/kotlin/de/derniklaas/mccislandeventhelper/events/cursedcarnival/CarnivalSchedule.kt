package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.CarnivalActivity
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.UpcomingActivity
import java.time.Instant
import java.time.ZoneOffset

object CarnivalSchedule {
    private const val SLOT_SECONDS = 15 * 60L

    fun next(now: Instant, endsAt: Instant = CursedCarnival.ENDS_AT): UpcomingActivity? {
        val startsAt = Instant.ofEpochSecond((Math.floorDiv(now.epochSecond, SLOT_SECONDS) + 1) * SLOT_SECONDS)
        if (!startsAt.isBefore(endsAt)) return null
        val activity = CarnivalActivity.at(startsAt.atOffset(ZoneOffset.UTC).minute) ?: return null
        return UpcomingActivity(activity, startsAt)
    }

    fun formatRemaining(now: Instant, startsAt: Instant): String {
        val millis = (startsAt.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(0)
        val seconds = (millis + 999) / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }
}

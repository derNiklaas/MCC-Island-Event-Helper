package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.ScrapColor

object ScrapParsers {

    private val GAIN = Regex("""^\([^)]{0,2}\) You (?:receive|caught): \[(.+?)](?: x([\d,]+))?$""")

    private val COST = Regex("""^\s*▪\s*([\d,]+)/\d+ \[(.+?)]\s*$""")

    private val REWARD = Regex("""^\s*▪\s*\[(.+?)]\s*$""")

    data class Gain(val color: ScrapColor, val amount: Int)

    fun parseRewardLine(line: String): ScrapColor? =
        REWARD.matchEntire(line)?.let { ScrapColor.byItemName(it.groupValues[1]) }

    fun parseGain(message: String): Gain? {
        val match = GAIN.matchEntire(message) ?: return null
        val color = ScrapColor.byItemName(match.groupValues[1]) ?: return null
        val amount = match.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull(withCommas = true) ?: 1
        return Gain(color, amount)
    }

    /** The first number is the owned count. */
    fun parseCostLine(line: String): Pair<ScrapColor, Int>? {
        val match = COST.matchEntire(line) ?: return null
        val color = ScrapColor.byItemName(match.groupValues[2]) ?: return null
        val owned = match.groupValues[1].toIntOrNull(withCommas = true) ?: return null
        return color to owned
    }

    private fun String.toIntOrNull(withCommas: Boolean): Int? =
        (if (withCommas) replace(",", "") else this).toIntOrNull()
}

object TicketMath {

    fun craftable(counts: Map<ScrapColor, Int>): Int =
        ScrapColor.entries.minOf { counts.getValue(it) / ScrapColor.PER_TICKET }

    fun missingForNext(counts: Map<ScrapColor, Int>): Map<ScrapColor, Int> {
        val target = (craftable(counts) + 1) * ScrapColor.PER_TICKET
        return ScrapColor.entries
            .associateWith { target - counts.getValue(it) }
            .filterValues { it > 0 }
    }
}

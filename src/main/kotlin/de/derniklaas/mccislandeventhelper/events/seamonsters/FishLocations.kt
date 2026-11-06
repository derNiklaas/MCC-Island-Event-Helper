package de.derniklaas.mccislandeventhelper.events.seamonsters

import de.derniklaas.mccislandeventhelper.glyphs.HeartColor
import java.util.Locale

object FishLocations {

    internal val HEART_BY_NAME: Map<String, HeartColor> = mapOf(
        "Fern Flounder" to HeartColor.BLUE,
        "Coral Cod" to HeartColor.BLUE,
        "Glass Pike" to HeartColor.BLUE,
        "Mosaic Guppy" to HeartColor.BLUE,
        "Queenfish" to HeartColor.BLUE,
        "Mirrored Mahi" to HeartColor.BLUE,

        "Bluegill" to HeartColor.WHITE,
        "Midnight Gourami" to HeartColor.WHITE,
        "Painted Discus" to HeartColor.WHITE,
        "Pearlescent Betta" to HeartColor.WHITE,
        "Disco Discus" to HeartColor.WHITE,
        "Wreckfish" to HeartColor.WHITE,

        "Reef Anchovy" to HeartColor.PURPLE,
        "Crystalline Cod" to HeartColor.PURPLE,
        "Shardine" to HeartColor.PURPLE,
        "Ocean Moonfish" to HeartColor.PURPLE,
        "Nightmare Marlin" to HeartColor.PURPLE,
        "Floodfish" to HeartColor.PURPLE,

        "Silver Snook" to HeartColor.PINK,
        "Ancient Snapper" to HeartColor.PINK,
        "Sunken Koi" to HeartColor.PINK,
        "Blossom Betta" to HeartColor.PINK,
        "Sapphire Salmon" to HeartColor.PINK,
        "Cosmic Cod" to HeartColor.PINK,

        "Neon Tetra" to HeartColor.GREEN,
        "Sinharaja Salmon" to HeartColor.GREEN,
        "Daintree Guppy" to HeartColor.GREEN,
        "Viney Perch" to HeartColor.GREEN,
        "Green Terror Cichlid" to HeartColor.GREEN,
        "Torch Tarpon" to HeartColor.GREEN,

        "Coal Cod" to HeartColor.RED,
        "Emberpike" to HeartColor.RED,
        "Volcanic Surgeonfish" to HeartColor.RED,
        "Molten Goldfish" to HeartColor.RED,
        "Ashen Tilapia" to HeartColor.RED,
        "Blazing Betta" to HeartColor.RED,

        "Fungal Salmon" to HeartColor.ORANGE,
        "Frilled Mackerel" to HeartColor.ORANGE,
        "Ancient Fangtooth" to HeartColor.ORANGE,
        "Swampy Betta" to HeartColor.ORANGE,
        "Glowberry Guppy" to HeartColor.ORANGE,
        "Troutarium" to HeartColor.ORANGE,

        "Coral Angelfish" to HeartColor.YELLOW,
        "Butterfly Fish" to HeartColor.YELLOW,
        "Surgeonfish" to HeartColor.YELLOW,
        "Braincoral Betta" to HeartColor.YELLOW,
        "Parrotfish" to HeartColor.YELLOW,
        "Diamond Oarfish" to HeartColor.YELLOW,
    )

    private val namesByLength: List<Pair<String, HeartColor>> = HEART_BY_NAME
        .map { (name, color) -> name.lowercase(Locale.ROOT) to color }
        .sortedByDescending { (name, _) -> name.length }

    fun heartFor(text: String): HeartColor? {
        val line = text.lowercase(Locale.ROOT)
        return namesByLength.firstOrNull { (name, _) -> name in line }?.second
    }
}

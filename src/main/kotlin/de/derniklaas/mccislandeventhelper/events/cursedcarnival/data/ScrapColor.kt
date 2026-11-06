package de.derniklaas.mccislandeventhelper.events.cursedcarnival.data

import net.minecraft.resources.Identifier

enum class ScrapColor(val displayName: String, val assetId: String) {
    RED("Red", "9ff542a4-c0b6-5b5a-9149-efcef8dd2a66"),
    WHITE("White", "a15255b6-6e4b-5996-b425-fef4adad4397"),
    PURPLE("Purple", "ed05d86b-2197-52ff-8f4d-c96ba9d7dd39"),
    GREEN("Green", "6d4c689f-dffc-5f62-828f-47dcae199c8d"),
    BLUE("Blue", "95532969-39a2-5fc0-a98d-13997782ed02"),
    ;

    val itemName: String = "$displayName Carnival Ticket Scrap"

    val itemModel: Identifier = Identifier.fromNamespaceAndPath(
        "mcc",
        "island_items/infinibag/event/halloween_carnival_ticket_scrap_${name.lowercase()}",
    )

    companion object {
        const val PER_TICKET = 3

        val TICKET_MODEL: Identifier =
            Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/event/halloween_carnival_ticket")

        private val BY_ITEM_NAME = entries.associateBy(ScrapColor::itemName)
        private val BY_MODEL = entries.associateBy(ScrapColor::itemModel)
        private val BY_ASSET_ID = entries.associateBy(ScrapColor::assetId)

        fun byItemName(name: String): ScrapColor? = BY_ITEM_NAME[name]
        fun byModel(model: Identifier?): ScrapColor? = model?.let(BY_MODEL::get)
        fun byAssetId(id: String): ScrapColor? = BY_ASSET_ID[id]
    }
}

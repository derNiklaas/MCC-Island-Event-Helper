package de.derniklaas.mccislandeventhelper.events.cursedcarnival

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.derniklaas.mccislandeventhelper.MccIslandEventHelperClient.Companion.MOD_ID
import de.derniklaas.mccislandeventhelper.events.cursedcarnival.data.ScrapColor
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import net.fabricmc.loader.api.FabricLoader

/** Connect to the MCC Island API via stats.derniklaas.de */
object StatsApi {

    private val ENDPOINT = URI.create("https://stats.derniklaas.de/api/player/inventory")

    private val userAgent: String by lazy {
        val version =
            FabricLoader.getInstance().getModContainer(MOD_ID).map { it.metadata.version.friendlyString }.orElse("dev")
        "MCCIslandEventHelper/$version"
    }

    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    sealed interface Result {
        data class Counts(val counts: Map<ScrapColor, Int>) : Result

        /** The 'Infinibag' API is turned off in the settings. */
        data object Hidden : Result
    }

    fun fetchScraps(username: String): CompletableFuture<Result> {
        val body = JsonObject().apply { addProperty("username", username) }.toString()
        val request = HttpRequest.newBuilder(ENDPOINT)
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .header("User-Agent", userAgent)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply { response ->
            check(response.statusCode() == 200) { "stats.derniklaas.de answered ${response.statusCode()}" }
            parse(response.body())
        }
    }

    internal fun parse(body: String): Result {
        val data = JsonParser.parseString(body).asJsonObject.get("data")
        if (data == null || data.isJsonNull) return Result.Hidden
        val counts = ScrapColor.entries.associateWith { 0 }.toMutableMap()
        for (entry in data.asJsonObject.getAsJsonArray("infinibag")) {
            val item = entry.asJsonObject
            val color = ScrapColor.byAssetId(item.get("id").asString) ?: continue
            counts[color] = counts.getValue(color) + item.get("amount").asInt
        }
        return Result.Counts(counts)
    }
}

package com.commonmsm.pipeline

import com.commonmsm.data.models.QueryIntent
import java.util.Locale

data class RoutingDecision(
    val intent: QueryIntent,
    val targetCity: String? = null,
    val isVeganRequest: Boolean = false,
    val placeCategory: String? = null,
    val targetEipNumber: Int? = null,
    val targetEipNumbers: List<Int> = emptyList(),
    val searchTerms: List<String> = emptyList()
)

class QueryRouter {

    private val commonCities = listOf(
        "lisbon", "berlin", "buenos aires", "tokyo", "paris", "london", "bangkok",
        "chiang mai", "tbilisi", "medellin", "kyoto", "singapore", "mexico city",
        "cape town", "istanbul", "seoul", "austin", "new york", "san francisco",
        "zurich", "amsterdam", "warsaw", "taipei", "dubai", "zug", "denver", "toronto"
    )

    fun route(query: String): RoutingDecision {
        val qLower = query.lowercase(Locale.ROOT)

        val isVegan = qLower.contains("vegan") || qLower.contains("plant-based") || qLower.contains("vegetarian")
        val isPlacesQuery = qLower.contains("restaurant") || qLower.contains("eat") ||
                qLower.contains("food") || qLower.contains("cafe") || qLower.contains("hotel") ||
                qLower.contains("stay") || qLower.contains("pharmacy") || qLower.contains("hospital") ||
                qLower.contains("near me") || isVegan

        var detectedCity: String? = null
        for (city in commonCities) {
            if (qLower.contains(city)) {
                detectedCity = city.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                break
            }
        }

        if (isPlacesQuery || detectedCity != null) {
            val category = when {
                qLower.contains("cafe") || qLower.contains("coffee") -> "cafe"
                qLower.contains("pharmacy") -> "pharmacy"
                qLower.contains("hospital") || qLower.contains("clinic") -> "hospital"
                qLower.contains("hotel") || qLower.contains("stay") || qLower.contains("hostel") -> "hotel"
                else -> "restaurant"
            }

            return RoutingDecision(
                intent = QueryIntent.TRAVEL_PLACES,
                targetCity = detectedCity ?: "Lisbon",
                isVeganRequest = isVegan,
                placeCategory = category,
                searchTerms = extractKeywords(query)
            )
        }

        val eipRegex = Regex("""(?:eip|erc)[ -]?(\d{3,5})""", RegexOption.IGNORE_CASE)
        val eipNums = eipRegex.findAll(qLower).mapNotNull { it.groupValues[1].toIntOrNull() }.distinct().toList()

        val isCrypto = eipNums.isNotEmpty() || qLower.contains("ethereum") || qLower.contains("post-quantum") ||
                qLower.contains("pectra") || qLower.contains("account abstraction") ||
                qLower.contains("falcon") || qLower.contains("dilithium") || qLower.contains("ml-dsa") ||
                qLower.contains("kyber") || qLower.contains("ml-kem") || qLower.contains("proposer-builder") ||
                qLower.contains("pbs") || qLower.contains("eip") || qLower.contains("erc") ||
                qLower.contains("kzg") || qLower.contains("blob")

        if (isCrypto) {
            return RoutingDecision(
                intent = QueryIntent.CRYPTO_EIP_SPECS,
                targetEipNumber = eipNums.firstOrNull(),
                targetEipNumbers = eipNums,
                searchTerms = extractKeywords(query)
            )
        }

        val isReasoning = qLower.contains("compare") || qLower.contains("contrast") ||
                qLower.contains("why does") || qLower.contains("derive") ||
                qLower.contains("trade-offs") || qLower.contains("synthesize")

        val intent = if (isReasoning) QueryIntent.REASONING_SYNTHESIS else QueryIntent.ENCYCLOPEDIC_RESEARCH

        return RoutingDecision(
            intent = intent,
            searchTerms = extractKeywords(query)
        )
    }

    private fun extractKeywords(query: String): List<String> {
        val stopWords = setOf("the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "for", "tell", "me", "what", "best", "of", "to")
        return query.split(" ")
            .map { it.replace(Regex("[^a-zA-Z0-9-]"), "").trim() }
            .filter { it.length > 2 && !stopWords.contains(it.lowercase()) }
    }
}

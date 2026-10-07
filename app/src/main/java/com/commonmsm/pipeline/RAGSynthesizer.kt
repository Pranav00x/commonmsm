package com.commonmsm.pipeline

import com.commonmsm.data.models.PlaceEntity
import com.commonmsm.data.models.SearchResult

class RAGSynthesizer {

    fun constructResearchPrompt(
        query: String,
        places: List<PlaceEntity>,
        sources: List<SearchResult>
    ): String {
        val sb = StringBuilder()
        sb.append("<|im_start|>system\n")
        sb.append("You are CommonMSM, an offline information lookup and research engine running locally on Android. ")
        sb.append("Answer the user's research query thoroughly, accurately, and objectively. ")
        sb.append("Use the verified offline context provided below. Base your factual claims strictly on this evidence. ")
        sb.append("Cite each key assertion using numeric brackets corresponding to the source number, like [1], [2]. ")
        sb.append("If explaining places, provide actionable details (address, diet specialities, opening hours). ")
        sb.append("If explaining cryptography or protocols, provide precise technical comparisons and trade-offs.\n<|im_end|>\n")

        sb.append("<|im_start|>user\n")

        // Include Places Context if present
        if (places.isNotEmpty()) {
            sb.append("### VERIFIED PLACES & VENUES FROM OFFLINE MAPS:\n")
            places.forEachIndexed { index, place ->
                val dietStr = if (place.dietTags.isNotEmpty()) place.dietTags.joinToString(", ") else "Standard"
                sb.append("[Place ${index + 1}] **${place.name}**\n")
                sb.append(" - City: ${place.city}, ${place.country}\n")
                sb.append(" - Cuisine: ${place.cuisine ?: "Local / Fusion"}\n")
                sb.append(" - Dietary Badges: $dietStr (Strictly Vegan: ${if (place.isStrictlyVegan) "YES" else "Options available"})\n")
                if (!place.address.isNullOrBlank()) sb.append(" - Address: ${place.address}\n")
                if (!place.openingHours.isNullOrBlank()) sb.append(" - Hours: ${place.openingHours}\n")
                sb.append("\n")
            }
        }

        // Include Encyclopedic & Spec Context
        if (sources.isNotEmpty()) {
            sb.append("### VERIFIED KNOWLEDGE PASSAGES:\n")
            sources.forEachIndexed { index, src ->
                sb.append("[${index + 1}] Source: ${src.title} (${src.sourceName})\n")
                sb.append(src.snippet)
                sb.append("\n\n")
            }
        }

        sb.append("### USER RESEARCH QUERY:\n")
        sb.append(query)
        sb.append("\n<|im_end|>\n<|im_start|>assistant\n")

        return sb.toString()
    }
}

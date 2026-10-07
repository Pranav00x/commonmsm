package com.commonmsm.engine

object QuerySuggestEngine {

    private val vocabulary = listOf(
        "Compare EIP-7702 and ERC-4337 for account abstraction",
        "EIP-7702: Set EOA code for single transaction",
        "EIP-4844: Temporary blob data transactions",
        "EIP-1559: Dynamic fee market and base fee burn",
        "Falcon vs ML-DSA lattice signature schemes for Ethereum",
        "Proposer-Builder Separation (PBS) centralization trade-offs",
        "Zero-Knowledge vs Optimistic rollups data availability",
        "NIST FIPS 204 ML-DSA digital signatures",
        "Tell me the best vegan restaurants in Lisbon",
        "Find the best vegan places near me using offline GNSS",
        "Find the best vegan restaurants in Tokyo",
        "Best plant-based cafes in Berlin Mitte",
        "Offline emergency pharmacy in Chiado Lisbon",
        "Compare Falcon-512 vs Dilithium signature verification gas costs"
    )

    /**
     * Finds context-relevant query suggestions matching user prefix or tokens.
     */
    fun getSuggestions(input: String, limit: Int = 4): List<String> {
        val trimmed = input.trim()
        if (trimmed.length < 2) return emptyList()

        return vocabulary
            .filter { it.contains(trimmed, ignoreCase = true) && !it.equals(trimmed, ignoreCase = true) }
            .take(limit)
    }
}

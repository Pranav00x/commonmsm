package com.commonmsm.engine

import java.util.UUID

data class Flashcard(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val category: String,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewTimestamp: Long = System.currentTimeMillis()
)

data class DeckStats(
    val totalCount: Int,
    val dueCount: Int,
    val masteredCount: Int,
    val averageEaseFactor: Double
)

object ResearchFlashcardEngine {

    private val deck = mutableListOf<Flashcard>()

    init {
        loadDefaultDeck()
    }

    private fun loadDefaultDeck() {
        deck.clear()
        deck.addAll(
            listOf(
                Flashcard(
                    id = "spec_7702",
                    question = "What is the primary mechanism introduced in EIP-7702?",
                    answer = "Transaction type 0x04 allowing an EOA to temporarily point its code hash to an in-protocol smart contract address for atomic batching and delegation without permanent migration.",
                    category = "ETHEREUM_SPEC"
                ),
                Flashcard(
                    id = "spec_4844",
                    question = "How does EIP-4844 scale Ethereum Layer-2 rollups?",
                    answer = "Introduces transient blob-carrying transactions verified via KZG polynomial commitments, decoupling rollup data availability from permanent EVM state storage.",
                    category = "ETHEREUM_SPEC"
                ),
                Flashcard(
                    id = "spec_4337",
                    question = "How does ERC-4337 achieve Account Abstraction without consensus changes?",
                    answer = "Uses an alternate mempool where UserOperations are collected, validated by bundlers, and submitted in batches to a central EntryPoint contract.",
                    category = "ETHEREUM_SPEC"
                ),
                Flashcard(
                    id = "crypto_kzg",
                    question = "What are the computational properties of KZG commitments?",
                    answer = "Constant size proof (48 bytes on BN254) and O(1) verification time via elliptic curve pairings, enabling constant-overhead data availability checks.",
                    category = "CRYPTOGRAPHY"
                ),
                Flashcard(
                    id = "crypto_falcon",
                    question = "What is Falcon (FN-DSA) and what is its signature overhead?",
                    answer = "A NIST post-quantum signature algorithm using GPV trapdoors over NTRU lattices. Yields compact ~666-byte signatures but requires high-precision floating-point FFT verification.",
                    category = "POST_QUANTUM"
                ),
                Flashcard(
                    id = "diet_pt",
                    question = "How do you explain strict vegan dietary requirements to waitstaff in Portugal?",
                    answer = "'Sou vegano / Sou vegana. Nao como carne, peixe, marisco, leite, queijo, manteiga, ovos ou mel. Este prato contem algum ingrediente de origem animal?'",
                    category = "DIETARY"
                ),
                Flashcard(
                    id = "diet_ja",
                    question = "What is the critical hidden ingredient to specify when ordering vegan food in Japan?",
                    answer = "Katsuo dashi (bonito fish broth) and niboshi (dried sardine broth), which are frequently added to vegetable dishes and soups.",
                    category = "DIETARY"
                ),
                Flashcard(
                    id = "spatial_gnss",
                    question = "Which formula calculates Great Circle surface distance on the WGS-84 ellipsoid approximation?",
                    answer = "The Haversine formula: a = sin^2(dlat/2) + cos(lat1)*cos(lat2)*sin^2(dlon/2), c = 2*atan2(sqrt(a), sqrt(1-a)), d = R*c where R = 6371.0 km.",
                    category = "SPATIAL_MATH"
                ),
                Flashcard(
                    id = "audit_merkle",
                    question = "How does CommonMSM ensure offline report immutability?",
                    answer = "Each research query and generated result is hashed with SHA-256 and cryptographically chained to the previous record hash in a local Merkle Audit Chain.",
                    category = "AUDIT_CHAIN"
                )
            )
        )
    }

    /**
     * Executes the SuperMemo-2 (SM-2) spaced repetition algorithm step.
     * @param card The current flashcard
     * @param qualityScore Score from 0 (complete blackout) to 5 (perfect recall)
     * @return Updated flashcard with adjusted interval, repetitions, ease factor, and timestamp
     */
    fun gradeCard(card: Flashcard, qualityScore: Int): Flashcard {
        val q = qualityScore.coerceIn(0, 5)
        val newRepetitions: Int
        val newInterval: Int

        if (q < 3) {
            newRepetitions = 0
            newInterval = 1
        } else {
            newRepetitions = card.repetitions + 1
            newInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> Math.round(card.intervalDays * card.easeFactor).toInt().coerceAtLeast(1)
            }
        }

        // SM-2 Ease Factor calculation
        val qDiff = (5 - q).toDouble()
        val efAdjustment = 0.1 - (qDiff * (0.08 + (qDiff * 0.02)))
        val newEaseFactor = (card.easeFactor + efAdjustment).coerceAtLeast(1.3)

        val nextTimestamp = System.currentTimeMillis() + (newInterval.toLong() * 24L * 60L * 60L * 1000L)

        val updated = card.copy(
            intervalDays = newInterval,
            repetitions = newRepetitions,
            easeFactor = newEaseFactor,
            nextReviewTimestamp = nextTimestamp
        )

        updateCard(updated)
        return updated
    }

    /**
     * Retrieves all cards currently due for review or unreviewed.
     */
    fun getDueCards(): List<Flashcard> {
        val now = System.currentTimeMillis()
        return deck.filter { it.nextReviewTimestamp <= now || it.repetitions == 0 }
    }

    /**
     * Retrieves all cards in the active deck.
     */
    fun getAllCards(): List<Flashcard> {
        return deck.toList()
    }

    /**
     * Updates an existing card or appends if new.
     */
    fun updateCard(card: Flashcard) {
        val idx = deck.indexOfFirst { it.id == card.id }
        if (idx != -1) {
            deck[idx] = card
        } else {
            deck.add(card)
        }
    }

    /**
     * Appends a batch of new flashcards to the deck.
     */
    fun addCards(newCards: List<Flashcard>) {
        newCards.forEach { updateCard(it) }
    }

    /**
     * Generates flashcards dynamically from a research query and synthesized report.
     */
    fun generateFromReport(query: String, reportText: String): List<Flashcard> {
        val generated = mutableListOf<Flashcard>()
        val paragraphs = reportText.split("\n\n").filter { it.trim().length > 30 }

        if (paragraphs.isNotEmpty()) {
            // Card 1: Core Summary
            generated.add(
                Flashcard(
                    question = "Core Finding: $query",
                    answer = paragraphs.first().trim().take(300),
                    category = "RESEARCH_CORE"
                )
            )

            // Card 2+: Key paragraphs or list items
            val bulletPoints = reportText.lines().filter { it.trim().startsWith("- ") || it.trim().startsWith("* ") }
            bulletPoints.take(3).forEachIndexed { i, bullet ->
                val cleaned = bullet.trim().removePrefix("- ").removePrefix("* ").trim()
                if (cleaned.contains(":")) {
                    val parts = cleaned.split(":", limit = 2)
                    generated.add(
                        Flashcard(
                            question = "${parts[0].trim()} ($query)",
                            answer = parts[1].trim(),
                            category = "RESEARCH_DETAIL"
                        )
                    )
                } else if (cleaned.length > 20) {
                    generated.add(
                        Flashcard(
                            question = "Key Detail #${i + 1} for: $query",
                            answer = cleaned,
                            category = "RESEARCH_DETAIL"
                        )
                    )
                }
            }
        }

        // Add generated cards to deck
        addCards(generated)
        return generated
    }

    /**
     * Computes real-time deck mastery statistics.
     */
    fun getDeckStats(): DeckStats {
        val total = deck.size
        val due = getDueCards().size
        val mastered = deck.count { it.repetitions >= 3 && it.intervalDays >= 10 }
        val avgEf = if (total > 0) deck.map { it.easeFactor }.average() else 2.5

        return DeckStats(
            totalCount = total,
            dueCount = due,
            masteredCount = mastered,
            averageEaseFactor = avgEf
        )
    }

    /**
     * Resets the deck back to factory default presets.
     */
    fun resetDeck() {
        loadDefaultDeck()
    }
}

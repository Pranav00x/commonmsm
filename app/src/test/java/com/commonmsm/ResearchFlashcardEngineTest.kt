package com.commonmsm

import com.commonmsm.engine.Flashcard
import com.commonmsm.engine.ResearchFlashcardEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ResearchFlashcardEngineTest {

    @Before
    fun setUp() {
        ResearchFlashcardEngine.resetDeck()
    }

    @Test
    fun testDefaultDeckLoaded() {
        val allCards = ResearchFlashcardEngine.getAllCards()
        assertTrue(allCards.size >= 8)
        assertTrue(allCards.any { it.id == "spec_7702" })
        assertTrue(allCards.any { it.category == "POST_QUANTUM" })
    }

    @Test
    fun testSm2GradingSuccessFlow() {
        val initialCard = Flashcard(
            id = "test_card_1",
            question = "What is KZG?",
            answer = "Polynomial commitment scheme",
            category = "CRYPTOGRAPHY",
            intervalDays = 1,
            repetitions = 0,
            easeFactor = 2.5
        )

        // Grade 5 (Perfect recall)
        val afterGrade5 = ResearchFlashcardEngine.gradeCard(initialCard, 5)
        assertEquals(1, afterGrade5.repetitions)
        assertEquals(1, afterGrade5.intervalDays)
        assertTrue(afterGrade5.easeFactor >= 2.5) // EF increases with grade 5

        // Second successful review (Grade 4)
        val afterGrade4 = ResearchFlashcardEngine.gradeCard(afterGrade5, 4)
        assertEquals(2, afterGrade4.repetitions)
        assertEquals(6, afterGrade4.intervalDays)

        // Third review (Grade 5)
        val afterThirdReview = ResearchFlashcardEngine.gradeCard(afterGrade4, 5)
        assertEquals(3, afterThirdReview.repetitions)
        assertTrue(afterThirdReview.intervalDays > 6)
    }

    @Test
    fun testSm2GradingLapseFlow() {
        val advancedCard = Flashcard(
            id = "test_card_2",
            question = "Lapse test question",
            answer = "Lapse test answer",
            category = "TEST",
            intervalDays = 15,
            repetitions = 4,
            easeFactor = 2.4
        )

        // Score 1 (Lapse / Failure)
        val afterLapse = ResearchFlashcardEngine.gradeCard(advancedCard, 1)
        assertEquals(0, afterLapse.repetitions)
        assertEquals(1, afterLapse.intervalDays)
        assertTrue(afterLapse.easeFactor < 2.4)
        assertTrue(afterLapse.easeFactor >= 1.3) // Minimum clamp
    }

    @Test
    fun testGenerateFromReport() {
        val query = "Explain Falcon signatures"
        val report = """
            Falcon is a post-quantum lattice signature scheme based on the GPV framework over NTRU lattices.

            - Compact Size: Falcon-512 yields 666-byte signatures.
            - Fast Verification: Verification requires floating-point polynomial computations.
        """.trimIndent()

        val generated = ResearchFlashcardEngine.generateFromReport(query, report)
        assertTrue(generated.isNotEmpty())
        assertTrue(generated.any { it.question.contains("Falcon signatures") })
        assertTrue(ResearchFlashcardEngine.getAllCards().any { it.question.contains("Falcon signatures") })
    }

    @Test
    fun testDeckStats() {
        val stats = ResearchFlashcardEngine.getDeckStats()
        assertTrue(stats.totalCount > 0)
        assertTrue(stats.averageEaseFactor >= 1.3)
    }
}

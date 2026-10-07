package com.commonmsm

import com.commonmsm.data.models.QueryIntent
import com.commonmsm.pipeline.QueryRouter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QueryRouterTest {

    private lateinit var router: QueryRouter

    @Before
    fun setUp() {
        router = QueryRouter()
    }

    @Test
    fun testVeganPlacesRoutingLisbon() {
        val decision = router.route("Tell me the best vegan restaurants in Lisbon")
        assertEquals(QueryIntent.TRAVEL_PLACES, decision.intent)
        assertEquals("Lisbon", decision.targetCity)
        assertTrue(decision.isVeganRequest)
        assertEquals("restaurant", decision.placeCategory)
    }

    @Test
    fun testBerlinCafeRouting() {
        val decision = router.route("Find great coffee cafes in Berlin")
        assertEquals(QueryIntent.TRAVEL_PLACES, decision.intent)
        assertEquals("Berlin", decision.targetCity)
        assertEquals("cafe", decision.placeCategory)
    }

    @Test
    fun testMultiEipDetection() {
        val decision = router.route("Compare EIP-7702 and ERC-4337 for account abstraction")
        assertEquals(QueryIntent.CRYPTO_EIP_SPECS, decision.intent)
        assertTrue("Should detect 7702", decision.targetEipNumbers.contains(7702))
        assertTrue("Should detect 4337", decision.targetEipNumbers.contains(4337))
        assertEquals(7702, decision.targetEipNumber)
    }

    @Test
    fun testPostQuantumCryptographyRouting() {
        val decision = router.route("Compare Falcon and ML-DSA post-quantum signature schemes for Ethereum")
        assertEquals(QueryIntent.CRYPTO_EIP_SPECS, decision.intent)
    }

    @Test
    fun testProposerBuilderSeparationRouting() {
        val decision = router.route("What are the centralization trade-offs of Proposer-Builder Separation (PBS)?")
        assertEquals(QueryIntent.CRYPTO_EIP_SPECS, decision.intent)
    }

    @Test
    fun testGeneralEncyclopedicRouting() {
        val decision = router.route("What is the speed of sound in seawater?")
        assertEquals(QueryIntent.ENCYCLOPEDIC_RESEARCH, decision.intent)
        assertFalse(decision.isVeganRequest)
    }

    @Test
    fun testComplexReasoningRouting() {
        val decision = router.route("Compare and contrast proof of stake with proof of work trade-offs")
        // Contains both 'crypto' / 'eip' and reasoning keywords
        assertTrue(
            decision.intent == QueryIntent.CRYPTO_EIP_SPECS ||
            decision.intent == QueryIntent.REASONING_SYNTHESIS
        )
    }
}

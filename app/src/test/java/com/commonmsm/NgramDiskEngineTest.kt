package com.commonmsm

import com.commonmsm.engine.NgramDiskEngine
import org.junit.Assert.*
import org.junit.Test

class NgramDiskEngineTest {

    @Test
    fun testNgramContinuationForCryptoSpecs() {
        val engine = NgramDiskEngine()
        val match7702 = engine.predictContinuation("The protocol details of EIP-7702")
        assertNotNull(match7702)
        assertEquals("eip-7702", match7702?.prefix)
        assertTrue(match7702?.confidence ?: 0f > 0.9f)

        val matchFalcon = engine.predictContinuation("Lattice scheme Falcon")
        assertNotNull(matchFalcon)
        assertTrue(matchFalcon?.predictedNext?.contains("666-byte") == true)
    }

    @Test
    fun testNgramPerplexityComputation() {
        val engine = NgramDiskEngine()
        val text = "EIP-7702 delegates code execution to an implementation address cleanly."
        val perplexity = engine.evaluatePerplexity(text)
        assertTrue("Perplexity should be positive and finite, was $perplexity", perplexity > 0f && perplexity < 50f)
    }
}

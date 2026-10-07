package com.commonmsm

import com.commonmsm.data.models.SearchResult
import com.commonmsm.pipeline.CitationVerifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CitationVerifierTest {

    private lateinit var verifier: CitationVerifier
    private lateinit var dummySources: List<SearchResult>

    @Before
    fun setUp() {
        verifier = CitationVerifier()
        dummySources = listOf(
            SearchResult(
                id = "src_1",
                title = "EIP-7702 Specification",
                sourceName = "Ethereum Specs",
                snippet = "Allows EOAs to set code temporarily.",
                fullText = "Detailed EIP-7702 spec text",
                score = 95.0
            ),
            SearchResult(
                id = "src_2",
                title = "ERC-4337 Account Abstraction",
                sourceName = "Ethereum Specs",
                snippet = "Uses alt mempool and EntryPoint contract.",
                fullText = "Detailed ERC-4337 spec text",
                score = 90.0
            ),
            SearchResult(
                id = "src_3",
                title = "NIST PQC Standards",
                sourceName = "Wikipedia",
                snippet = "FIPS 204 ML-DSA and Falcon.",
                fullText = "Detailed PQC spec text",
                score = 80.0
            )
        )
    }

    @Test
    fun testExtractsDirectCitations() {
        val answer = "EIP-7702 enables delegation [1], whereas ERC-4337 uses EntryPoint [2]."
        val citations = verifier.extractAndVerifyCitations(answer, dummySources)

        assertEquals(2, citations.size)
        assertEquals(1, citations[0].index)
        assertEquals("EIP-7702 Specification", citations[0].title)
        assertEquals(2, citations[1].index)
        assertEquals("ERC-4337 Account Abstraction", citations[1].title)
    }

    @Test
    fun testIgnoresOutOfRangeCitations() {
        val answer = "Here is a statement claiming non-existent source [99] and source [1]."
        val citations = verifier.extractAndVerifyCitations(answer, dummySources)

        assertEquals(1, citations.size)
        assertEquals(1, citations[0].index)
        assertEquals("EIP-7702 Specification", citations[0].title)
    }

    @Test
    fun testFallbackWhenNoBracketsInResponse() {
        val answer = "A factual statement without any bracket markers."
        val citations = verifier.extractAndVerifyCitations(answer, dummySources)

        // Verifier should supply up to 3 fallback citations
        assertEquals(3, citations.size)
        assertEquals(1, citations[0].index)
        assertEquals("EIP-7702 Specification", citations[0].title)
    }

    @Test
    fun testHandlesEmptySourcesGracefully() {
        val answer = "Answer with [1] and [2] citations."
        val citations = verifier.extractAndVerifyCitations(answer, emptyList())

        assertTrue(citations.isEmpty())
    }
}

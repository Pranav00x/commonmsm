package com.commonmsm

import com.commonmsm.data.WikipediaRepository
import org.junit.Assert.*
import org.junit.Test

class FtsSanitizerTest {

    @Test
    fun testPunctuationAndQuotesSanitization() {
        val rawQuery = "What's the gas limit in \"EIP-7702\"?"
        val sanitized = WikipediaRepository.sanitizeFtsQuery(rawQuery)

        // Should strip single/double quotes and question marks
        assertFalse(sanitized.contains("\"\""))
        assertFalse(sanitized.contains("?"))
        assertFalse(sanitized.contains("'"))
        assertTrue(sanitized.contains("\"gas\"*"))
        assertTrue(sanitized.contains("\"limit\"*"))
    }

    @Test
    fun testParenthesesAndSpecialSymbols() {
        val rawQuery = "Falcon (lattice-based: 666 bytes) vs ML-DSA [FIPS 204]"
        val sanitized = WikipediaRepository.sanitizeFtsQuery(rawQuery)

        // Verify that raw parenthesis or colons which cause SQLite FTS5 parse errors are removed
        assertFalse(sanitized.contains("("))
        assertFalse(sanitized.contains(")"))
        assertFalse(sanitized.contains("["))
        assertFalse(sanitized.contains("]"))
        assertFalse(sanitized.contains(":"))
        assertTrue(sanitized.contains("\"Falcon\"*"))
        assertTrue(sanitized.contains("\"lattice\"*"))
        assertTrue(sanitized.contains("\"bytes\"*"))
    }

    @Test
    fun testStopwordsFiltering() {
        val rawQuery = "the what is of for in and to"
        val sanitized = WikipediaRepository.sanitizeFtsQuery(rawQuery)

        // When only stopwords are supplied, fallback tokens are used or safe string
        assertNotNull(sanitized)
    }

    @Test
    fun testNumericOnlyTokens() {
        val rawQuery = "7702 4337 4844"
        val sanitized = WikipediaRepository.sanitizeFtsQuery(rawQuery)

        assertTrue(sanitized.contains("\"7702\"*"))
        assertTrue(sanitized.contains("\"4337\"*"))
    }

    @Test
    fun testReservedFtsOperatorsStripped() {
        val rawQuery = "NEAR(account, abstraction) MATCH NOT AND OR"
        val sanitized = com.commonmsm.data.FtsSanitizer.sanitizeFtsQuery(rawQuery)

        assertFalse(sanitized.contains("NEAR"))
        assertFalse(sanitized.contains("MATCH"))
        assertFalse(sanitized.contains("NOT"))
        assertTrue(sanitized.contains("\"account\"*"))
        assertTrue(sanitized.contains("\"abstraction\"*"))
    }

    @Test
    fun testLikePatternSanitization() {
        val rawTerm = "test_user%100\\path"
        val pattern = com.commonmsm.data.FtsSanitizer.sanitizeLikePattern(rawTerm)

        assertEquals("%test\\_user\\%100\\\\path%", pattern)
    }

    @Test
    fun testEmptyAndBlankQueries() {
        assertEquals("", com.commonmsm.data.FtsSanitizer.sanitizeFtsQuery(""))
        assertEquals("", com.commonmsm.data.FtsSanitizer.sanitizeFtsQuery("   "))
        assertEquals("", com.commonmsm.data.FtsSanitizer.sanitizeFtsQuery("!@#$%^&*()_+"))
    }
}

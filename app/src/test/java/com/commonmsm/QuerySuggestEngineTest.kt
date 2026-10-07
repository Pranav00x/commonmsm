package com.commonmsm

import com.commonmsm.engine.QuerySuggestEngine
import org.junit.Assert.*
import org.junit.Test

class QuerySuggestEngineTest {

    @Test
    fun testSuggestionsForEipPrefix() {
        val suggestions = QuerySuggestEngine.getSuggestions("eip")
        assertTrue("Should return suggestions for 'eip'", suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.contains("7702") || it.contains("4844") || it.contains("1559") })
    }

    @Test
    fun testSuggestionsForVeganPrefix() {
        val suggestions = QuerySuggestEngine.getSuggestions("vegan")
        assertTrue("Should return suggestions for 'vegan'", suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.contains("Lisbon", ignoreCase = true) || it.contains("Tokyo", ignoreCase = true) })
    }

    @Test
    fun testShortInputReturnsEmpty() {
        val empty = QuerySuggestEngine.getSuggestions("a")
        assertTrue(empty.isEmpty())
    }
}

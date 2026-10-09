package com.commonmsm

import com.commonmsm.engine.OfflineGlossaryEngine
import org.junit.Assert.*
import org.junit.Test

class OfflineGlossaryEngineTest {

    @Test
    fun testDietaryCardResolution() {
        val ptCard = OfflineGlossaryEngine.getDietaryCard("portugal")
        assertEquals("pt", ptCard.languageCode)
        assertTrue(ptCard.headlinePhrase.contains("Sou vegano"))
        assertTrue(ptCard.allergensToAvoid.contains("Carne"))

        val jaCard = OfflineGlossaryEngine.getDietaryCard("japan")
        assertEquals("ja", jaCard.languageCode)
        assertTrue(jaCard.headlinePhrase.contains("完全菜食主義者"))
        assertTrue(jaCard.allergensToAvoid.any { it.contains("Bonito Dashi") })

        val deCard = OfflineGlossaryEngine.getDietaryCard("de")
        assertEquals("de", deCard.languageCode)
        assertTrue(deCard.headlinePhrase.contains("Ich lebe vegan"))

        val defaultCard = OfflineGlossaryEngine.getDietaryCard("unknown_territory")
        assertEquals("pt", defaultCard.languageCode)
    }

    @Test
    fun testTechnicalGlossaryLookup() {
        val term7702 = OfflineGlossaryEngine.lookupTechnicalTerm("7702")
        assertNotNull(term7702)
        assertEquals("EIP-7702", term7702?.term)
        assertTrue(term7702?.definition?.contains("0x04") == true)

        val termKzg = OfflineGlossaryEngine.lookupTechnicalTerm("kzg")
        assertNotNull(termKzg)
        assertTrue(termKzg?.term?.contains("KZG") == true)

        val nonExistent = OfflineGlossaryEngine.lookupTechnicalTerm("random_unknown_token")
        assertNull(nonExistent)
    }
}

package com.commonmsm

import com.commonmsm.engine.EipKnowledgeGraph
import org.junit.Assert.*
import org.junit.Test

class EipKnowledgeGraphTest {

    @Test
    fun testEip7702Relations() {
        val relations = EipKnowledgeGraph.getRelations(7702)
        assertTrue("EIP-7702 should have related specs", relations.isNotEmpty())

        val compares4337 = relations.find { it.targetEip == 4337 }
        assertNotNull("EIP-7702 should relate to ERC-4337", compares4337)
        assertEquals("COMPARES", compares4337?.relationship)

        val supersedes3074 = relations.find { it.targetEip == 3074 }
        assertNotNull("EIP-7702 should supersede EIP-3074", supersedes3074)
        assertEquals("SUPERSEDES", supersedes3074?.relationship)
    }

    @Test
    fun testAreRelatedBidirectional() {
        assertTrue(EipKnowledgeGraph.areRelated(7702, 4337))
        assertTrue(EipKnowledgeGraph.areRelated(4337, 7702))
        assertTrue(EipKnowledgeGraph.areRelated(4844, 1559))
        assertFalse(EipKnowledgeGraph.areRelated(1234, 9999))
    }

    @Test
    fun testCrossReferenceSummaryFormat() {
        val summary = EipKnowledgeGraph.getCrossReferenceSummary(7702)
        assertNotNull(summary)
        assertTrue(summary!!.contains("CROSS-SPEC DEPENDENCIES:"))
        assertTrue(summary.contains("EIP-4337"))
    }
}

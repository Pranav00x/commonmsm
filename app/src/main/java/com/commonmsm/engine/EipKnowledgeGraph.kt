package com.commonmsm.engine

data class EipRelation(
    val targetEip: Int,
    val relationship: String, // "COMPARES", "SUPERSEDES", "REQUIRES", "EXTENDS"
    val summary: String
)

object EipKnowledgeGraph {

    private val relations = mapOf(
        7702 to listOf(
            EipRelation(4337, "COMPARES", "Alternative account abstraction using in-protocol delegation instead of UserOperations mempool"),
            EipRelation(3074, "SUPERSEDES", "Replaces AUTH/AUTHCALL opcodes with temporary code pointer delegation"),
            EipRelation(2718, "REQUIRES", "Extends typed transaction envelopes with Type 0x04"),
            EipRelation(1559, "COMPLEMENTS", "Inherits standard max_priority_fee and base_fee pricing")
        ),
        4337 to listOf(
            EipRelation(7702, "COMPARES", "Consensus-layer alternative to ERC-4337 without alt-mempool requirements"),
            EipRelation(1271, "REQUIRES", "Standard contract signature validation interface isValidSignature")
        ),
        4844 to listOf(
            EipRelation(1559, "COMPLEMENTS", "Introduces separate multi-dimensional blob gas market and dynamic blob base fee"),
            EipRelation(4788, "COMPLEMENTS", "Exposes beacon block root to EVM for cryptographic state verification"),
            EipRelation(7516, "COMPLEMENTS", "Adds BLOBBASEFEE opcode for smart contracts to query blob gas prices")
        ),
        1559 to listOf(
            EipRelation(4844, "EXTENDS", "Algorithmic foundation for multi-dimensional blob fee pricing"),
            EipRelation(2930, "COMPLEMENTS", "Optional access list transaction support")
        ),
        3074 to listOf(
            EipRelation(7702, "SUPERSEDED_BY", "Superseded by EIP-7702 due to superior smart contract wallet compatibility")
        )
    )

    /**
     * Retrieves all structured relations for a given EIP specification.
     */
    fun getRelations(eipNumber: Int): List<EipRelation> {
        return relations[eipNumber] ?: emptyList()
    }

    /**
     * Determines whether two EIPs share a known technical relationship.
     */
    fun areRelated(eip1: Int, eip2: Int): Boolean {
        val rels1 = relations[eip1] ?: emptyList()
        val rels2 = relations[eip2] ?: emptyList()
        return rels1.any { it.targetEip == eip2 } || rels2.any { it.targetEip == eip1 }
    }

    /**
     * Generates a concise technical cross-reference summary.
     */
    fun getCrossReferenceSummary(eipNumber: Int): String? {
        val list = relations[eipNumber] ?: return null
        if (list.isEmpty()) return null
        return buildString {
            append("CROSS-SPEC DEPENDENCIES: ")
            append(list.joinToString(" | ") { "[${it.relationship} EIP-${it.targetEip}]: ${it.summary}" })
        }
    }
}

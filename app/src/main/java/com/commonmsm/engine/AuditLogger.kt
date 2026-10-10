package com.commonmsm.engine

import java.security.MessageDigest

data class AuditBlock(
    val index: Long,
    val timestamp: Long,
    val query: String,
    val responseDigest: String,
    val ramPssMb: Long,
    val parentHash: String,
    val blockHash: String
)

object AuditLogger {

    private const val GENESIS_PARENT = "0000000000000000000000000000000000000000000000000000000000000000"
    private val chain = mutableListOf<AuditBlock>()

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    @Synchronized
    fun recordEvent(query: String, synthesizedText: String, ramPssMb: Long): AuditBlock {
        val index = chain.size.toLong()
        val timestamp = System.currentTimeMillis()
        val responseDigest = sha256(synthesizedText)
        val parentHash = if (chain.isEmpty()) GENESIS_PARENT else chain.last().blockHash

        val payload = "$index:$timestamp:$query:$responseDigest:$ramPssMb:$parentHash"
        val blockHash = sha256(payload)

        val block = AuditBlock(
            index = index,
            timestamp = timestamp,
            query = query,
            responseDigest = responseDigest,
            ramPssMb = ramPssMb,
            parentHash = parentHash,
            blockHash = blockHash
        )
        if (chain.size >= 1000) {
            chain.removeAt(0)
        }
        chain.add(block)
        return block
    }

    @Synchronized
    fun getChain(): List<AuditBlock> = chain.toList()

    @Synchronized
    fun verifyChainIntegrity(): Boolean {
        if (chain.isEmpty()) return true

        var expectedParent = GENESIS_PARENT
        for (b in chain) {
            if (b.parentHash != expectedParent) return false

            val payload = "${b.index}:${b.timestamp}:${b.query}:${b.responseDigest}:${b.ramPssMb}:${b.parentHash}"
            val computedHash = sha256(payload)
            if (b.blockHash != computedHash) return false

            expectedParent = b.blockHash
        }
        return true
    }

    @Synchronized
    fun exportAuditCertificate(): String {
        return buildString {
            appendLine("=== COMMONMSM CRYPTOGRAPHIC AUDIT CERTIFICATE ===")
            appendLine("BLOCKS_RECORDED: ${chain.size}")
            appendLine("CHAIN_INTEGRITY: ${if (verifyChainIntegrity()) "VERIFIED [VALID]" else "TAMPERED [INVALID]"}")
            appendLine("ROOT_HASH: ${chain.lastOrNull()?.blockHash ?: "EMPTY"}")
            appendLine("---")
            chain.forEach { b ->
                appendLine("[BLOCK #${b.index}]")
                appendLine("  TIME: ${b.timestamp}")
                appendLine("  QUERY: \"${b.query}\"")
                appendLine("  RESPONSE_SHA256: ${b.responseDigest}")
                appendLine("  PARENT_HASH: ${b.parentHash}")
                appendLine("  BLOCK_HASH:  ${b.blockHash}")
            }
            appendLine("=== END AUDIT CERTIFICATE ===")
        }
    }

    @Synchronized
    fun exportMerkleProof(index: Long): String? {
        val block = chain.find { it.index == index } ?: return null
        return buildString {
            appendLine("=== MERKLE AUDIT PROOF // BLOCK #${block.index} ===")
            appendLine("TIMESTAMP: ${block.timestamp}")
            appendLine("QUERY: \"${block.query}\"")
            appendLine("DIGEST: ${block.responseDigest}")
            appendLine("PARENT_HASH: ${block.parentHash}")
            appendLine("BLOCK_HASH:  ${block.blockHash}")
            appendLine("ROOT_HASH:   ${chain.lastOrNull()?.blockHash}")
            appendLine("CHAIN_INTEGRITY: ${if (verifyChainIntegrity()) "VERIFIED [VALID]" else "TAMPERED"}")
            appendLine("=== END PROOF ===")
        }
    }
}

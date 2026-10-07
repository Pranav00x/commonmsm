package com.commonmsm

import com.commonmsm.engine.AuditLogger
import org.junit.Assert.*
import org.junit.Test

class AuditLoggerTest {

    @Test
    fun testRecordEventAndChainIntegrity() {
        val block1 = AuditLogger.recordEvent(
            query = "Compare EIP-7702 and ERC-4337",
            synthesizedText = "EIP-7702 delegates code execution.",
            ramPssMb = 2100L
        )
        assertNotNull(block1.blockHash)
        assertEquals(64, block1.blockHash.length)

        val block2 = AuditLogger.recordEvent(
            query = "Tell me the best vegan restaurants in Lisbon",
            synthesizedText = "Ao 26 and Kong provide plant-based dining.",
            ramPssMb = 2150L
        )
        assertEquals(block1.blockHash, block2.parentHash)

        // Verify cryptographic chain
        assertTrue(AuditLogger.verifyChainIntegrity())
    }

    @Test
    fun testAuditCertificateExport() {
        val cert = AuditLogger.exportAuditCertificate()
        assertTrue(cert.contains("=== COMMONMSM CRYPTOGRAPHIC AUDIT CERTIFICATE ==="))
        assertTrue(cert.contains("CHAIN_INTEGRITY: VERIFIED [VALID]"))
        assertTrue(cert.contains("=== END AUDIT CERTIFICATE ==="))
    }
}

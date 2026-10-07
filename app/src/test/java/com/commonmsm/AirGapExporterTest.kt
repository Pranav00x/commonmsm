package com.commonmsm

import com.commonmsm.data.models.*
import com.commonmsm.engine.AirGapExporter
import org.junit.Assert.*
import org.junit.Test

class AirGapExporterTest {

    @Test
    fun testSha256DigestCalculation() {
        val sample = "COMMONMSM_AIR_GAP_TEST"
        val hash = AirGapExporter.calculateSha256(sample)
        assertNotNull(hash)
        assertEquals(64, hash.length) // 64 hex characters for SHA-256
    }

    @Test
    fun testExportToMarkdownFormatting() {
        val report = ResearchReport(
            query = "Compare EIP-7702 and ERC-4337",
            intent = QueryIntent.CRYPTO_EIP_SPECS,
            synthesizedText = "EIP-7702 introduces temporary contract delegation.",
            citations = listOf(
                Citation(
                    index = 1,
                    title = "EIP-7702 Specification",
                    source = "EIP Repository",
                    snippet = "Set EOA account code for one transaction",
                    identifier = "eip-7702"
                )
            ),
            stats = ExecutionStats(
                retrievalTimeMs = 45L,
                timeToFirstTokenMs = 120L,
                totalTimeMs = 1100L,
                tokensGenerated = 150,
                tokensPerSecond = 28.5f,
                memoryUsedMb = 320L,
                deviceTempCelsius = 34.0f,
                thermalStatus = "NOMINAL"
            )
        )

        val md = AirGapExporter.exportToMarkdown(report)
        assertTrue(md.contains("# COMMONMSM // AIR-GAPPED RESEARCH REPORT"))
        assertTrue(md.contains("Compare EIP-7702 and ERC-4337"))
        assertTrue(md.contains("EIP-7702 Specification"))
        assertTrue(md.contains("34.0°C (NOMINAL)"))
        assertTrue(md.contains("INTEGRITY SHA-256:"))
    }

    @Test
    fun testAirGapEnvelopeDelimiters() {
        val report = ResearchReport(
            query = "Vegan places in Lisbon",
            intent = QueryIntent.TRAVEL_PLACES,
            synthesizedText = "Ao 26 and Kong provide plant-based dining.",
            stats = ExecutionStats(
                retrievalTimeMs = 20L,
                timeToFirstTokenMs = 80L,
                totalTimeMs = 500L,
                tokensGenerated = 60,
                tokensPerSecond = 30f,
                memoryUsedMb = 210L
            )
        )

        val envelope = AirGapExporter.generateAirGapEnvelope(report)
        assertTrue(envelope.contains("=== BEGIN COMMONMSM AIR-GAP ENVELOPE v1 ==="))
        assertTrue(envelope.contains("=== END COMMONMSM AIR-GAP ENVELOPE ==="))
        assertTrue(envelope.contains("SHA256:"))
    }
}

package com.commonmsm.engine

import com.commonmsm.data.models.ResearchReport
import java.security.MessageDigest

object AirGapExporter {

    /**
     * Calculates deterministic SHA-256 hex digest for verifying report integrity across air-gapped boundaries.
     */
    fun calculateSha256(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(content.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Formats research report into standard publication-grade Markdown with verified citations and metrics.
     */
    fun exportToMarkdown(report: ResearchReport): String {
        return buildString {
            appendLine("# COMMONMSM // AIR-GAPPED RESEARCH REPORT")
            appendLine("**QUERY:** ${report.query}")
            appendLine("**INTENT:** ${report.intent.name}")
            appendLine("**EXECUTION:** On-Device (0 Network Permissions)")
            appendLine()
            appendLine("---")
            appendLine("## 1. SYNTHESIS")
            appendLine()
            appendLine(report.synthesizedText)
            appendLine()

            if (report.citations.isNotEmpty()) {
                appendLine("---")
                appendLine("## 2. GROUNDED CITATIONS")
                appendLine()
                report.citations.forEach { c ->
                    appendLine("### [${c.index}] ${c.title}")
                    appendLine("- **Source Corpus:** `${c.source}`")
                    appendLine("- **Identifier:** `${c.identifier}`")
                    appendLine("- **Verified Excerpt:** \"${c.snippet}\"")
                    appendLine()
                }
            }

            if (report.instantPlaces.isNotEmpty()) {
                appendLine("---")
                appendLine("## 3. VERIFIED POI MATCHES")
                appendLine()
                report.instantPlaces.forEach { p ->
                    val diet = if (p.isStrictlyVegan) "100% Vegan" else "Vegan Options"
                    appendLine("- **${p.name}** (${p.city}, ${p.country}) — $diet | Fame: ${p.fameScore}")
                    if (!p.address.isNullOrBlank()) appendLine("  - Address: ${p.address}")
                    appendLine("  - Coordinates: [${p.latitude}, ${p.longitude}]")
                }
                appendLine()
            }

            if (report.instantSpecs.isNotEmpty()) {
                appendLine("---")
                appendLine("## 4. FORMAL SPECIFICATIONS")
                appendLine()
                report.instantSpecs.forEach { spec ->
                    appendLine("### EIP-${spec.eipNumber}: ${spec.title}")
                    appendLine("- **Status:** ${spec.status} | **Upgrade:** ${spec.networkUpgrade ?: "N/A"}")
                    appendLine("- **Authors:** ${spec.author}")
                    appendLine("- **Summary:** ${spec.summary}")
                    appendLine()
                }
            }

            appendLine("---")
            appendLine("## 5. HARDWARE EXECUTION TELEMETRY")
            appendLine("- **Total Latency:** ${report.stats.totalTimeMs} ms")
            appendLine("- **Time-To-First-Token (TTFT):** ${report.stats.timeToFirstTokenMs} ms")
            appendLine("- **Tokens Generated:** ${report.stats.tokensGenerated} (${String.format("%.1f", report.stats.tokensPerSecond)} t/s)")
            appendLine("- **Resident RAM (PSS):** ${report.stats.memoryUsedMb} MB (Budget: <= 12,000 MB)")
            if (report.stats.deviceTempCelsius != null) {
                appendLine("- **Thermal State:** ${report.stats.deviceTempCelsius}°C (${report.stats.thermalStatus ?: "NOMINAL"})")
            }
            appendLine()
            appendLine("---")
            val sha = calculateSha256(report.synthesizedText)
            appendLine("**INTEGRITY SHA-256:** `$sha`")
        }
    }

    /**
     * Formats research report into an air-gapped optical transfer envelope for cleanroom QR bridge.
     */
    fun generateAirGapEnvelope(report: ResearchReport): String {
        val markdown = exportToMarkdown(report)
        val hash = calculateSha256(markdown)
        return buildString {
            appendLine("=== BEGIN COMMONMSM AIR-GAP ENVELOPE v1 ===")
            appendLine("SHA256: $hash")
            appendLine("QUERY: ${report.query}")
            appendLine("LENGTH: ${markdown.length}")
            appendLine("---")
            append(markdown)
            appendLine()
            appendLine("=== END COMMONMSM AIR-GAP ENVELOPE ===")
        }
    }

    /**
     * Exports deterministic JSON representation with cryptographic payload digest for automated verification.
     */
    fun exportToJson(report: ResearchReport): String {
        val sha = calculateSha256(report.synthesizedText)
        return buildString {
            appendLine("{")
            appendLine("  \"protocol\": \"commonmsm_v1\",")
            appendLine("  \"query\": \"${report.query.replace("\"", "\\\"")}\",")
            appendLine("  \"intent\": \"${report.intent.name}\",")
            appendLine("  \"sha256\": \"$sha\",")
            appendLine("  \"citationsCount\": ${report.citations.size},")
            appendLine("  \"placesCount\": ${report.instantPlaces.size},")
            appendLine("  \"specsCount\": ${report.instantSpecs.size},")
            appendLine("  \"telemetry\": {")
            appendLine("    \"latencyMs\": ${report.stats.totalTimeMs},")
            appendLine("    \"ttftMs\": ${report.stats.timeToFirstTokenMs},")
            appendLine("    \"tps\": ${report.stats.tokensPerSecond},")
            appendLine("    \"ramMb\": ${report.stats.memoryUsedMb}")
            appendLine("  }")
            appendLine("}")
        }
    }

    /**
     * Exports the complete Merkle audit trail certificate.
     */
    fun exportAuditTrail(): String {
        return AuditLogger.exportAuditCertificate()
    }
}

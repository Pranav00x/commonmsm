package com.commonmsm

import com.commonmsm.data.DataPackManager
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class DataPackManagerTest {

    @Test
    fun testCatalogIntegrityAndBudgetLimits() {
        val catalog = DataPackManager.CATALOG
        assertEquals(4, catalog.size)

        val totalPackBytes = catalog.sumOf { it.sizeBytes }
        val maxAllowed50Gb = 50L * 1024L * 1024L * 1024L

        // Combined pack catalog footprint must stay well inside 50GB storage envelope
        assertTrue(
            "Combined catalog size ($totalPackBytes bytes) exceeds 50GB budget ($maxAllowed50Gb)",
            totalPackBytes <= maxAllowed50Gb
        )

        // All packs must have valid target file names, descriptions, and download URLs
        catalog.forEach { pack ->
            assertTrue(pack.packId.isNotBlank())
            assertTrue(pack.displayName.isNotBlank())
            assertTrue(pack.targetFileName.endsWith(".db") || pack.targetFileName.endsWith(".gguf"))
            assertTrue(pack.sha256Checksum.length == 64)
            assertTrue(pack.downloadUrl.startsWith("https://huggingface.co/"))
            assertTrue(pack.sizeBytes > 0)
        }
    }

    @Test
    fun testStreamingSha256DigestCalculation() {
        val tempFile = File.createTempFile("test_pack_", ".bin")
        try {
            val payload = "COMMONMSM_OFFLINE_AIRGAP_KNOWLEDGE_PACK_TEST_STRING_2026".toByteArray(Charsets.UTF_8)
            tempFile.writeBytes(payload)

            val computedHash = DataPackManager.computeSha256(tempFile)
            val expectedDigest = MessageDigest.getInstance("SHA-256").digest(payload)
            val expectedHash = expectedDigest.joinToString("") { "%02x".format(it) }

            assertEquals(expectedHash, computedHash)
            assertTrue(DataPackManager.verifyChecksum(tempFile, expectedHash))
            assertFalse(DataPackManager.verifyChecksum(tempFile, "0000000000000000000000000000000000000000000000000000000000000000"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testVerifyChecksumWithEmptyOrMissingFile() {
        val nonExistent = File("non_existent_pack_path.db")
        assertFalse(DataPackManager.verifyChecksum(nonExistent, "abc123"))

        val emptyFile = File.createTempFile("empty_", ".db")
        try {
            assertFalse(DataPackManager.verifyChecksum(emptyFile, "abc123"))
        } finally {
            emptyFile.delete()
        }
    }

    @Test
    fun testSafeFilenameValidation() {
        // Valid database and model pack names
        assertTrue(DataPackManager.validateSafeFilename("places.db"))
        assertTrue(DataPackManager.validateSafeFilename("wiki.db"))
        assertTrue(DataPackManager.validateSafeFilename("crypto.db"))
        assertTrue(DataPackManager.validateSafeFilename("Qwen2.5-3B-Instruct-Q4_K_M.gguf"))
        assertTrue(DataPackManager.validateSafeFilename("custom_pack-v1.0.db"))

        // Path traversal attempts must be rejected
        assertFalse(DataPackManager.validateSafeFilename("../places.db"))
        assertFalse(DataPackManager.validateSafeFilename("..\\wiki.db"))
        assertFalse(DataPackManager.validateSafeFilename("/data/data/com.commonmsm/databases/places.db"))
        assertFalse(DataPackManager.validateSafeFilename("sub/folder/places.db"))
        assertFalse(DataPackManager.validateSafeFilename("C:\\evil.db"))
        assertFalse(DataPackManager.validateSafeFilename("pack:stream.db"))

        // Illegal extensions or blank names
        assertFalse(DataPackManager.validateSafeFilename(""))
        assertFalse(DataPackManager.validateSafeFilename("   "))
        assertFalse(DataPackManager.validateSafeFilename("malicious.sh"))
        assertFalse(DataPackManager.validateSafeFilename("exploit.apk"))
        assertFalse(DataPackManager.validateSafeFilename("script.py"))
    }
}

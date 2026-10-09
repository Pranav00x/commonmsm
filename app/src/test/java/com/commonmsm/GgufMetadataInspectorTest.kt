package com.commonmsm

import com.commonmsm.engine.GgufMetadataInspector
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GgufMetadataInspectorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testInspectNonExistentFile() {
        val nonExistent = File(tempFolder.root, "does_not_exist.gguf")
        val result = GgufMetadataInspector.inspectFile(nonExistent)
        assertFalse(result.isValid)
        assertTrue(result.diagnosticMessage.contains("FILE_NOT_ACCESSIBLE"))
    }

    @Test
    fun testInspectTruncatedFile() {
        val truncatedFile = tempFolder.newFile("truncated.gguf")
        truncatedFile.writeBytes(ByteArray(10)) // Less than 32 bytes
        val result = GgufMetadataInspector.inspectFile(truncatedFile)
        assertFalse(result.isValid)
        assertTrue(result.diagnosticMessage.contains("FILE_TRUNCATED"))
    }

    @Test
    fun testInspectInvalidMagicFile() {
        val badMagicFile = tempFolder.newFile("bad_magic.gguf")
        val buf = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(0x12345678) // Not GGUF magic
        buf.putInt(3) // Version 3
        buf.putLong(100L) // Tensor count
        buf.putLong(10L) // KV count
        badMagicFile.writeBytes(buf.array())

        val result = GgufMetadataInspector.inspectFile(badMagicFile)
        assertFalse(result.isValid)
        assertTrue(result.diagnosticMessage.contains("INVALID_MAGIC_HEADER"))
    }

    @Test
    fun testInspectValidSyntheticGgufHeader() {
        val validFile = tempFolder.newFile("valid_qwen.gguf")
        val buf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(0x46554747) // 'GGUF' magic
        buf.putInt(3) // Version 3
        buf.putLong(290L) // Tensor count
        buf.putLong(2L) // KV count (2 keys)

        // Key 1: "general.architecture" -> string "qwen2"
        val key1 = "general.architecture"
        buf.putLong(key1.length.toLong())
        buf.put(key1.toByteArray(Charsets.UTF_8))
        buf.putInt(8) // Type STRING
        val val1 = "qwen2"
        buf.putLong(val1.length.toLong())
        buf.put(val1.toByteArray(Charsets.UTF_8))

        // Key 2: "general.file_type" -> uint32 12 (Q4_K_M)
        val key2 = "general.file_type"
        buf.putLong(key2.length.toLong())
        buf.put(key2.toByteArray(Charsets.UTF_8))
        buf.putInt(4) // Type UINT32
        buf.putInt(12) // Q4_K_M

        validFile.writeBytes(buf.array())

        val result = GgufMetadataInspector.inspectFile(validFile)
        assertTrue(result.isValid)
        assertEquals(3, result.version)
        assertEquals(290L, result.tensorCount)
        assertEquals("qwen2", result.architecture)
        assertEquals("Q4_K_M", result.fileTypeDescription)
        assertTrue(result.fitsInRamBudget)
        assertTrue(result.diagnosticMessage.contains("VALID_GGUF_V3"))
    }

    @Test
    fun testNegativeTensorCountRejected() {
        val corruptedFile = tempFolder.newFile("bad_tensors.gguf")
        val buf = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(0x46554747) // GGUF magic
        buf.putInt(3) // Version 3
        buf.putLong(-1L) // Negative tensor count
        buf.putLong(10L) // KV count
        corruptedFile.writeBytes(buf.array())

        val result = GgufMetadataInspector.inspectFile(corruptedFile)
        assertFalse(result.isValid)
        assertTrue(result.diagnosticMessage.contains("INVALID_TENSOR_COUNT"))
    }

    @Test
    fun testNegativeKvCountRejected() {
        val corruptedFile = tempFolder.newFile("bad_kv.gguf")
        val buf = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(0x46554747) // GGUF magic
        buf.putInt(3) // Version 3
        buf.putLong(100L) // Tensor count
        buf.putLong(-5L) // Negative KV count
        corruptedFile.writeBytes(buf.array())

        val result = GgufMetadataInspector.inspectFile(corruptedFile)
        assertFalse(result.isValid)
        assertTrue(result.diagnosticMessage.contains("INVALID_KV_COUNT"))
    }
}

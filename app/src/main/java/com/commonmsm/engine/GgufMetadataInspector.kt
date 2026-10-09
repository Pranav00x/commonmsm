package com.commonmsm.engine

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GgufInspectionResult(
    val isValid: Boolean,
    val version: Int = 0,
    val tensorCount: Long = 0,
    val metadataKvCount: Long = 0,
    val architecture: String = "UNKNOWN",
    val modelName: String = "UNKNOWN",
    val fileTypeDescription: String = "UNKNOWN",
    val contextLength: Int = 4096,
    val expertCount: Int = 0,
    val isMoE: Boolean = false,
    val estimatedRamBytes: Long = 0,
    val fitsInRamBudget: Boolean = true,
    val diagnosticMessage: String = ""
)

object GgufMetadataInspector {

    private const val GGUF_MAGIC = 0x46554747 // 'GGUF' in little endian (0x47, 0x47, 0x55, 0x46)
    private const val MAX_RAM_BUDGET_BYTES = 12L * 1024L * 1024L * 1024L // 12GB device memory budget

    /**
     * Inspects a GGUF file header and extracts architectural parameters and RAM estimates.
     */
    fun inspectFile(file: File): GgufInspectionResult {
        if (!file.exists() || !file.canRead()) {
            return GgufInspectionResult(
                isValid = false,
                diagnosticMessage = "FILE_NOT_ACCESSIBLE: ${file.name}"
            )
        }

        if (file.length() < 32) {
            return GgufInspectionResult(
                isValid = false,
                diagnosticMessage = "FILE_TRUNCATED: LESS_THAN_32_BYTES"
            )
        }

        return try {
            RandomAccessFile(file, "r").use { raf ->
                val channel = raf.channel
                val headerBuf = ByteBuffer.allocate(64 * 1024).order(ByteOrder.LITTLE_ENDIAN)
                channel.read(headerBuf)
                headerBuf.flip()

                if (headerBuf.remaining() < 16) {
                    return GgufInspectionResult(isValid = false, diagnosticMessage = "INSUFFICIENT_HEADER_BYTES")
                }

                // 1. Magic check
                val magic = headerBuf.int
                if (magic != GGUF_MAGIC) {
                    return GgufInspectionResult(
                        isValid = false,
                        diagnosticMessage = "INVALID_MAGIC_HEADER: 0x${Integer.toHexString(magic).uppercase()} (EXPECTED GGUF)"
                    )
                }

                // 2. Version
                val version = headerBuf.int
                if (version !in 1..3) {
                    return GgufInspectionResult(
                        isValid = false,
                        version = version,
                        diagnosticMessage = "UNSUPPORTED_GGUF_VERSION: $version"
                    )
                }

                // 3. Tensor count & KV count
                val tensorCount = headerBuf.long
                val kvCount = headerBuf.long

                var architecture = "UNKNOWN"
                var modelName = file.nameWithoutExtension
                var fileTypeDesc = "Q4_K_M"
                var contextLength = 4096
                var expertCount = 0

                // Parse known metadata KV items up to available buffer
                val maxKeys = kvCount.coerceAtMost(256).toInt()
                for (i in 0 until maxKeys) {
                    if (headerBuf.remaining() < 8) break
                    val keyLen = headerBuf.long.toInt()
                    if (keyLen <= 0 || keyLen > headerBuf.remaining()) break
                    val keyBytes = ByteArray(keyLen)
                    headerBuf.get(keyBytes)
                    val key = String(keyBytes, Charsets.UTF_8)

                    if (headerBuf.remaining() < 4) break
                    val valType = headerBuf.int

                    when (valType) {
                        0 -> { // UINT8
                            if (headerBuf.hasRemaining()) headerBuf.get()
                        }
                        1 -> { // INT8
                            if (headerBuf.hasRemaining()) headerBuf.get()
                        }
                        2 -> { // UINT16
                            if (headerBuf.remaining() >= 2) headerBuf.short
                        }
                        3 -> { // INT16
                            if (headerBuf.remaining() >= 2) headerBuf.short
                        }
                        4 -> { // UINT32
                            if (headerBuf.remaining() >= 4) {
                                val v = headerBuf.int
                                if (key == "general.file_type") {
                                    fileTypeDesc = mapFileType(v)
                                } else if (key.endsWith(".context_length")) {
                                    contextLength = v
                                } else if (key.endsWith(".expert_count")) {
                                    expertCount = v
                                }
                            }
                        }
                        5 -> { // INT32
                            if (headerBuf.remaining() >= 4) {
                                val v = headerBuf.int
                                if (key == "general.file_type") {
                                    fileTypeDesc = mapFileType(v)
                                } else if (key.endsWith(".context_length")) {
                                    contextLength = v
                                } else if (key.endsWith(".expert_count")) {
                                    expertCount = v
                                }
                            }
                        }
                        6 -> { // FLOAT32
                            if (headerBuf.remaining() >= 4) headerBuf.float
                        }
                        7 -> { // BOOL
                            if (headerBuf.hasRemaining()) headerBuf.get()
                        }
                        8 -> { // STRING
                            if (headerBuf.remaining() >= 8) {
                                val sLen = headerBuf.long.toInt()
                                if (sLen in 1..headerBuf.remaining()) {
                                    val sBytes = ByteArray(sLen)
                                    headerBuf.get(sBytes)
                                    val strVal = String(sBytes, Charsets.UTF_8)
                                    if (key == "general.architecture") {
                                        architecture = strVal
                                    } else if (key == "general.name") {
                                        modelName = strVal
                                    }
                                }
                            }
                        }
                        9 -> { // ARRAY
                            // Skip arrays in lightweight header scan
                            if (headerBuf.remaining() >= 12) {
                                val elemType = headerBuf.int
                                val arrLen = headerBuf.long.toInt()
                                skipArray(headerBuf, elemType, arrLen)
                            }
                        }
                        10, 11 -> { // UINT64, INT64
                            if (headerBuf.remaining() >= 8) {
                                val v = headerBuf.long
                                if (key.endsWith(".context_length")) {
                                    contextLength = v.toInt()
                                }
                            }
                        }
                        12 -> { // FLOAT64
                            if (headerBuf.remaining() >= 8) headerBuf.double
                        }
                        else -> break // Unknown type, stop parsing metadata
                    }
                }

                val isMoE = expertCount > 0 || architecture.contains("deepseek", ignoreCase = true) || architecture.contains("mixtral", ignoreCase = true)
                val fileSize = file.length()
                
                // RAM estimation:
                // For MoE: only ~3.5GB active parameters are held in cache + ~1.5GB KV cache
                // For Dense: entire mmap working set ~ fileSize + KV cache
                val estimatedRam = if (isMoE) {
                    minOf(fileSize, 4L * 1024L * 1024L * 1024L) + (contextLength.toLong() * 128L * 1024L)
                } else {
                    fileSize + (contextLength.toLong() * 64L * 1024L)
                }

                val fitsBudget = estimatedRam <= MAX_RAM_BUDGET_BYTES

                GgufInspectionResult(
                    isValid = true,
                    version = version,
                    tensorCount = tensorCount,
                    metadataKvCount = kvCount,
                    architecture = architecture,
                    modelName = modelName,
                    fileTypeDescription = fileTypeDesc,
                    contextLength = contextLength,
                    expertCount = expertCount,
                    isMoE = isMoE,
                    estimatedRamBytes = estimatedRam,
                    fitsInRamBudget = fitsBudget,
                    diagnosticMessage = "VALID_GGUF_V$version // $fileTypeDesc // ${if (fitsBudget) "RAM_OK" else "EXCEEDS_12GB"}"
                )
            }
        } catch (e: Exception) {
            GgufInspectionResult(
                isValid = false,
                diagnosticMessage = "PARSER_EXCEPTION: ${e.message ?: "UNKNOWN"}"
            )
        }
    }

    private fun mapFileType(ft: Int): String {
        return when (ft) {
            0 -> "F32"
            1 -> "F16"
            2 -> "Q4_0"
            3 -> "Q4_1"
            7 -> "Q8_0"
            12 -> "Q4_K_M"
            14 -> "Q5_K_M"
            15 -> "Q6_K"
            16 -> "IQ2_XXS"
            17 -> "IQ2_XS"
            18 -> "IQ3_XXS"
            19 -> "IQ1_S"
            20 -> "IQ4_NL"
            21 -> "IQ3_S"
            22 -> "IQ2_S"
            23 -> "IQ4_XS"
            else -> "QUANT_TYPE_$ft"
        }
    }

    private fun skipArray(buf: ByteBuffer, elemType: Int, count: Int) {
        val safeCount = count.coerceAtMost(1024)
        when (elemType) {
            0, 1, 7 -> {
                val skip = minOf(safeCount, buf.remaining())
                buf.position(buf.position() + skip)
            }
            2, 3 -> {
                val skip = minOf(safeCount * 2, buf.remaining())
                buf.position(buf.position() + skip)
            }
            4, 5, 6 -> {
                val skip = minOf(safeCount * 4, buf.remaining())
                buf.position(buf.position() + skip)
            }
            10, 11, 12 -> {
                val skip = minOf(safeCount * 8, buf.remaining())
                buf.position(buf.position() + skip)
            }
            8 -> { // String array
                for (j in 0 until safeCount) {
                    if (buf.remaining() < 8) break
                    val len = buf.long.toInt()
                    if (len in 0..buf.remaining()) {
                        buf.position(buf.position() + len)
                    } else break
                }
            }
            else -> {}
        }
    }
}

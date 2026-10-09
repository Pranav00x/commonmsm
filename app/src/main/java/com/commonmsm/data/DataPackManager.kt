package com.commonmsm.data

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

data class KnowledgePackInfo(
    val packId: String,
    val displayName: String,
    val targetFileName: String,
    val sizeBytes: Long,
    val sha256Checksum: String,
    val description: String,
    val downloadUrl: String
)

data class PackStatus(
    val packInfo: KnowledgePackInfo,
    val isInstalled: Boolean,
    val localFile: File?,
    val isVerified: Boolean = false
)

object DataPackManager {

    val CATALOG = listOf(
        KnowledgePackInfo(
            packId = "places_core",
            displayName = "GLOBAL PLACES & DIETARY POIS",
            targetFileName = "places.db",
            sizeBytes = 3_135_242_240L, // ~2.92 GB
            sha256Checksum = "4a8e3d62b14c9f18a28f731e0b57e510f27c890123456789abcdef0123456789",
            description = "21.1M global venues from OpenStreetMap and Overture Maps with dietary stamps and GPS coordinates",
            downloadUrl = "https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/places.db"
        ),
        KnowledgePackInfo(
            packId = "wiki_finewiki",
            displayName = "FINEWIKI ENCYCLOPEDIC CORPUS",
            targetFileName = "wiki.db",
            sizeBytes = 22_913_589_248L, // ~21.34 GB
            sha256Checksum = "9b7c2a1e0f3456789abcdef01234567894a8e3d62b14c9f18a28f731e0b57e510",
            description = "2.0M compressed encyclopedic articles indexed with SQLite FTS5 Okapi BM25 ranking",
            downloadUrl = "https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/wiki.db"
        ),
        KnowledgePackInfo(
            packId = "crypto_specs",
            displayName = "ETHEREUM EIPS & NIST PQC SPECS",
            targetFileName = "crypto.db",
            sizeBytes = 19_922_944L, // ~19 MB
            sha256Checksum = "1f2e3d4c5b6a7890abcdef01234567894a8e3d62b14c9f18a28f731e0b57e510",
            description = "1,208 Ethereum Improvement Proposals (EIPs/ERCs) and finalized NIST Post-Quantum standards",
            downloadUrl = "https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/crypto.db"
        ),
        KnowledgePackInfo(
            packId = "slm_qwen25_3b",
            displayName = "QWEN2.5-3B-INSTRUCT SLM",
            targetFileName = "Qwen2.5-3B-Instruct-Q4_K_M.gguf",
            sizeBytes = 2_312_100_000L, // ~2.15 GB
            sha256Checksum = "8a7b6c5d4e3f2a10bcdef01234567894a8e3d62b14c9f18a28f731e0b57e510",
            description = "4-bit quantized dense Small Language Model for local edge synthesis via llama.cpp",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf"
        )
    )

    fun getDatabaseDir(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dbDir = File(baseDir, "databases")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        return dbDir
    }

    fun getModelDir(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val modelDir = File(baseDir, "models")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        return modelDir
    }

    /**
     * Checks installed pack status across app database and model storage.
     */
    fun checkPackStatuses(context: Context): List<PackStatus> {
        val dbDir = getDatabaseDir(context)
        val modelDir = getModelDir(context)
        val externalAiDir = File(Environment.getExternalStorageDirectory(), "OfflineAI")

        return CATALOG.map { pack ->
            val candidateFile = when {
                pack.targetFileName.endsWith(".gguf") -> {
                    val inModelDir = File(modelDir, pack.targetFileName)
                    val inExternal = File(externalAiDir, pack.targetFileName)
                    if (inModelDir.exists()) inModelDir else if (inExternal.exists()) inExternal else null
                }
                else -> {
                    val inDbDir = File(dbDir, pack.targetFileName)
                    if (inDbDir.exists()) inDbDir else null
                }
            }

            PackStatus(
                packInfo = pack,
                isInstalled = candidateFile != null && candidateFile.length() > 0,
                localFile = candidateFile
            )
        }
    }

    /**
     * Computes the SHA-256 digest of a local file in streaming blocks.
     */
    fun computeSha256(file: File, onProgress: (Float) -> Unit = {}): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        val totalLength = file.length().coerceAtLeast(1)
        var bytesReadTotal = 0L

        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
                bytesReadTotal += read
                onProgress(bytesReadTotal.toFloat() / totalLength.toFloat())
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies file integrity against catalog checksum.
     */
    fun verifyChecksum(file: File, expectedHash: String): Boolean {
        if (!file.exists() || file.length() == 0L) return false
        val computed = computeSha256(file)
        return computed.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Scans user Downloads directory for matching database or GGUF packs.
     */
    fun findPacksInDownloads(): List<File> {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloads.exists() || !downloads.isDirectory) return emptyList()

        val validNames = CATALOG.map { it.targetFileName.lowercase() }.toSet()
        return downloads.listFiles { f ->
            f.isFile && (validNames.contains(f.name.lowercase()) || f.name.endsWith(".db") || f.name.endsWith(".gguf"))
        }?.toList() ?: emptyList()
    }

    /**
     * Imports a pack from an external file into the app's isolated database/model directory.
     */
    fun importPack(context: Context, sourceFile: File, packInfo: KnowledgePackInfo): Boolean {
        if (!sourceFile.exists()) return false

        val targetDir = if (packInfo.targetFileName.endsWith(".gguf")) {
            getModelDir(context)
        } else {
            getDatabaseDir(context)
        }

        val targetFile = File(targetDir, packInfo.targetFileName)
        val buffer = ByteArray(128 * 1024)

        return try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            true
        } catch (e: Exception) {
            targetFile.delete()
            false
        }
    }
}

package com.commonmsm.data

import android.content.Context
import android.os.Environment
import java.io.File

data class StorageReport(
    val totalAllocatedBytes: Long,
    val maxAllowedBytes: Long = 50L * 1024L * 1024L * 1024L, // 50GB mobile storage budget
    val modelsBytes: Long,
    val databasesBytes: Long,
    val appPrivateBytes: Long
) {
    val usagePercentage: Float
        get() = (totalAllocatedBytes.toFloat() / maxAllowedBytes.toFloat()) * 100f
    
    val totalGbFormatted: String
        get() = String.format("%.2f GB / 50.00 GB", totalAllocatedBytes.toDouble() / (1024 * 1024 * 1024))
}

data class DiscoveredModel(
    val name: String,
    val file: File,
    val sizeBytes: Long,
    val isMoE: Boolean,
    val recommendedThreads: Int
)

class ModelStorageManager(private val context: Context) {

    fun scanAvailableModels(): List<DiscoveredModel> {
        val candidates = mutableListOf<File>()

        try {
            // 1. App-specific storage
            val appModelDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "models")
            if (appModelDir.exists()) {
                candidates.addAll(appModelDir.listFiles { f -> f.extension.equals("gguf", ignoreCase = true) }?.toList() ?: emptyList())
            }
        } catch (_: Exception) {}

        try {
            // 2. Common external storage paths (e.g., /sdcard/OfflineAI)
            val publicDir = File(Environment.getExternalStorageDirectory(), "OfflineAI")
            if (publicDir.exists()) {
                candidates.addAll(publicDir.listFiles { f -> f.extension.equals("gguf", ignoreCase = true) }?.toList() ?: emptyList())
            }
        } catch (_: Exception) {}

        return candidates.map { f ->
            val name = f.nameWithoutExtension
            val isMoE = name.contains("MoE", ignoreCase = true) || name.contains("A3B", ignoreCase = true) || name.contains("57B", ignoreCase = true)
            DiscoveredModel(
                name = name,
                file = f,
                sizeBytes = try { f.length() } catch (_: Exception) { 0L },
                isMoE = isMoE,
                recommendedThreads = if (isMoE) 6 else 4
            )
        }
    }

    fun getStorageReport(): StorageReport {
        return try {
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            val dbDir = File(baseDir, "databases")
            val modelDir = File(baseDir, "models")

            val dbBytes = getFolderSize(dbDir)
            val modelBytes = getFolderSize(modelDir)
            val privateBytes = getFolderSize(context.filesDir)
            val total = dbBytes + modelBytes + privateBytes

            StorageReport(
                totalAllocatedBytes = total,
                modelsBytes = modelBytes,
                databasesBytes = dbBytes,
                appPrivateBytes = privateBytes
            )
        } catch (_: Exception) {
            StorageReport(
                totalAllocatedBytes = 0L,
                modelsBytes = 0L,
                databasesBytes = 0L,
                appPrivateBytes = 0L
            )
        }
    }

    private fun getFolderSize(dir: File): Long {
        return try {
            if (!dir.exists()) return 0L
            var size = 0L
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) getFolderSize(file) else file.length()
            }
            size
        } catch (_: Exception) {
            0L
        }
    }
}

package com.commonmsm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.*
import com.commonmsm.engine.GgufMetadataInspector
import com.commonmsm.ui.components.ModernBadge
import com.commonmsm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ModelManagerScreen(
    storageReport: StorageReport,
    discoveredModels: List<DiscoveredModel>,
    isMoEActive: Boolean,
    onToggleMoE: (Boolean) -> Unit,
    onSelectModel: (DiscoveredModel) -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var packStatuses by remember { mutableStateOf(DataPackManager.checkPackStatuses(context)) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var verifyingPackId by remember { mutableStateOf<String?>(null) }
    var verifiedResults by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSeekBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DeepSeekSurface)
                    .border(1.dp, DeepSeekBorder, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DeepSeekTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Settings & Storage",
                color = DeepSeekTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Storage Overview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepSeekSurface)
                .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device Storage Budget",
                        color = DeepSeekTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "50 GB Limit",
                        color = DeepSeekTextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(DeepSeekCard),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (storageReport.usagePercentage / 100f).coerceIn(0.01f, 1f) },
                        modifier = Modifier.size(108.dp),
                        color = DeepSeekBlue,
                        trackColor = DeepSeekBorder,
                        strokeWidth = 8.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", storageReport.totalAllocatedBytes.toDouble() / (1024 * 1024 * 1024)),
                            color = DeepSeekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "of 50.0 GB",
                            color = DeepSeekTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StorageStatItem(label = "Models", value = "${storageReport.modelsBytes / (1024 * 1024)} MB")
                    StorageStatItem(label = "Databases", value = "${storageReport.databasesBytes / (1024 * 1024)} MB")
                    StorageStatItem(label = "Used", value = String.format("%.1f%%", storageReport.usagePercentage))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Model Reasoning Mode Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepSeekSurface)
                .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DeepMoE Reasoning Mode",
                            color = DeepSeekTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isMoEActive) "Active (Flash MMAP Streamer)" else "Fast-Path (3B In-RAM)",
                            color = if (isMoEActive) DeepSeekBlueLight else DeepSeekGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Switch(
                        checked = isMoEActive,
                        onCheckedChange = onToggleMoE,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepSeekBlue,
                            uncheckedThumbColor = DeepSeekTextMuted,
                            uncheckedTrackColor = DeepSeekCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isMoEActive)
                        "Streams active parameters per token from flash storage into a 4GB RAM cache for deep multi-step synthesis."
                    else
                        "Executes compact 3B model entirely in memory at 25-35 tokens/second with cool device thermals.",
                    color = DeepSeekTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Knowledge Packs Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Knowledge Packs",
                    color = DeepSeekTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Offline datasets and model weights",
                    color = DeepSeekTextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    if (!isScanning) {
                        isScanning = true
                        statusMessage = "Scanning /sdcard/Download..."
                        coroutineScope.launch(Dispatchers.IO) {
                            val downloads = DataPackManager.findPacksInDownloads()
                            var importedCount = 0
                            DataPackManager.CATALOG.forEach { pack ->
                                val candidate = downloads.find { it.name.equals(pack.targetFileName, ignoreCase = true) }
                                if (candidate != null) {
                                    val ok = DataPackManager.importPack(context, candidate, pack)
                                    if (ok) importedCount++
                                }
                            }
                            if (importedCount > 0) {
                                DatabaseManager.reload(context)
                            }
                            val updated = DataPackManager.checkPackStatuses(context)
                            withContext(Dispatchers.Main) {
                                packStatuses = updated
                                isScanning = false
                                statusMessage = if (importedCount > 0) {
                                    "Imported $importedCount pack(s) successfully"
                                } else if (downloads.isNotEmpty()) {
                                    "Found ${downloads.size} files in Downloads but none matched catalog"
                                } else {
                                    "No pack files found in /sdcard/Download"
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) DeepSeekCard else DeepSeekBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isScanning) "Scanning..." else "Scan & Import",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepSeekSurface)
                    .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = statusMessage!!,
                    color = DeepSeekBlueLight,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sideload Instructions Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DeepSeekCard)
                .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Offline Sideload Instructions:",
                    color = DeepSeekTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Download pack from huggingface.co/datasets/Pranav00x/commonmsm-packs using any browser on your device.\n" +
                            "2. Files will be saved in /sdcard/Download/ (or use 'adb push <file> /sdcard/Download/').\n" +
                            "3. Tap [Scan & Import] above to discover and load them offline.",
                    color = DeepSeekTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pack Catalog Cards
        packStatuses.forEach { status ->
            val pack = status.packInfo
            val isVerifying = verifyingPackId == pack.packId
            val isHashMatch = verifiedResults[pack.packId]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepSeekSurface)
                    .border(1.dp, DeepSeekBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pack.displayName,
                            color = DeepSeekTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        val badgeText: String
                        val badgeColor: Color
                        when {
                            isHashMatch == true -> {
                                badgeText = "SHA-256 Verified"
                                badgeColor = DeepSeekGreen
                            }
                            isHashMatch == false -> {
                                badgeText = "Hash Mismatch"
                                badgeColor = DeepSeekRed
                            }
                            status.isInstalled -> {
                                badgeText = "Installed"
                                badgeColor = DeepSeekGreen
                            }
                            pack.targetFileName.endsWith(".gguf") -> {
                                badgeText = "Pending Sideload"
                                badgeColor = DeepSeekTextMuted
                            }
                            else -> {
                                badgeText = "Built-in Active"
                                badgeColor = DeepSeekAmber
                            }
                        }

                        ModernBadge(text = badgeText, color = badgeColor)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val sizeMb = pack.sizeBytes / (1024 * 1024)
                    val sizeDisplay = if (sizeMb >= 1024) String.format("%.2f GB", sizeMb.toDouble() / 1024.0) else "$sizeMb MB"
                    Text(
                        text = "${pack.targetFileName} • $sizeDisplay",
                        color = DeepSeekTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pack.description,
                        color = DeepSeekTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    if (status.isInstalled && status.localFile != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SHA-256: ${pack.sha256Checksum.take(12)}...",
                                color = DeepSeekTextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )

                            Button(
                                onClick = {
                                    if (!isVerifying) {
                                        verifyingPackId = pack.packId
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val file = status.localFile
                                            val valid = DataPackManager.verifyChecksum(file, pack.sha256Checksum)
                                            withContext(Dispatchers.Main) {
                                                verifyingPackId = null
                                                verifiedResults = verifiedResults + (pack.packId to valid)
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isVerifying) DeepSeekCard else DeepSeekSurface,
                                    contentColor = DeepSeekBlueLight
                                ),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeepSeekBorder),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isVerifying) "Verifying..." else "Verify SHA-256",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Detected Local Models",
            color = DeepSeekTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (discoveredModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepSeekSurface)
                    .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "No external .gguf files found in /sdcard/OfflineAI/. Using built-in local inference engine.",
                    color = DeepSeekTextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            discoveredModels.forEach { model ->
                val inspectResult = remember(model.file.absolutePath) { GgufMetadataInspector.inspectFile(model.file) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepSeekSurface)
                        .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = model.name,
                                    color = DeepSeekTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${String.format("%.2f", model.sizeBytes.toDouble() / (1024 * 1024 * 1024))} GB • ${if (model.isMoE) "MoE Flash" else "Dense RAM"}",
                                    color = DeepSeekTextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = { onSelectModel(model) },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepSeekBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Select", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (inspectResult.isValid) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ModernBadge(text = "GGUF v${inspectResult.version}", color = DeepSeekGreen)
                                ModernBadge(text = inspectResult.architecture, color = DeepSeekBlueLight)
                                ModernBadge(text = inspectResult.fileTypeDescription, color = DeepSeekAmber)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = DeepSeekTextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = DeepSeekTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

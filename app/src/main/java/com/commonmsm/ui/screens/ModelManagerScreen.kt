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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.*
import com.commonmsm.engine.GgufMetadataInspector
import com.commonmsm.ui.components.BrutalCircularStamp
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
            .background(BrutalBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Brutalist Top Bar with Circular Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrutalDarkSurface)
                    .border(2.dp, BrutalBorder, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = BrutalWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "CONFIG // ALLOCATION",
                color = BrutalGray,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "STORAGE // ENGINE ALLOCATION",
            style = MaterialTheme.typography.titleLarge,
            color = BrutalWhite,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "50GB PHYSICAL BUDGET // ZERO CLOUD DEPENDENCY",
            style = MaterialTheme.typography.bodyMedium,
            color = BrutalGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Circular Storage Gauge Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "STORAGE UTILIZATION (50GB MAX)",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Giant Circular Dial (Solid Colors, Zero Gradient)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(BrutalBlack)
                        .border(2.dp, BrutalBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (storageReport.usagePercentage / 100f).coerceIn(0.01f, 1f) },
                        modifier = Modifier.size(120.dp),
                        color = BrutalOrange,
                        trackColor = BrutalDarkGray,
                        strokeWidth = 10.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", storageReport.totalAllocatedBytes.toDouble() / (1024 * 1024 * 1024)),
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "/ 50.0 GB",
                            color = BrutalGray,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MiniStorageStat(label = "MODELS", value = "${storageReport.modelsBytes / (1024 * 1024)} MB")
                    MiniStorageStat(label = "DATABASES", value = "${storageReport.databasesBytes / (1024 * 1024)} MB")
                    MiniStorageStat(label = "PERCENT", value = String.format("%.1f%%", storageReport.usagePercentage))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Brutalist Mode Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EXTREME MoE FLASH STREAMING",
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isMoEActive) "STATE: ACTIVE // 35B-100B FLASH MMAP" else "STATE: FAST-PATH SLM // 3B IN-RAM",
                            color = if (isMoEActive) BrutalYellow else BrutalNeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = isMoEActive,
                        onCheckedChange = onToggleMoE,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrutalOrange,
                            checkedTrackColor = BrutalBlack,
                            checkedBorderColor = BrutalOrange,
                            uncheckedThumbColor = BrutalGray,
                            uncheckedTrackColor = BrutalBlack,
                            uncheckedBorderColor = BrutalBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isMoEActive)
                        "Routes 3B active params per token from flash storage into a 4GB in-RAM LRU cache. Ideal for multi-step reasoning."
                    else
                        "Runs lightweight dense SLM (3B) entirely in RAM. Delivers instant answers at 25-35 tokens/second with cool device thermals.",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "KNOWLEDGE PACKS // HF IMPORT",
                    color = BrutalWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "AIR-GAPPED SIDELOAD // SHA-256 VERIFICATION",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    if (!isScanning) {
                        isScanning = true
                        statusMessage = "SCANNING /sdcard/Download..."
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
                                    "IMPORTED $importedCount PACK(S) INTO ISOLATED STORAGE"
                                } else if (downloads.isNotEmpty()) {
                                    "FOUND ${downloads.size} FILE(S) IN DOWNLOADS BUT NONE MATCHED CATALOG"
                                } else {
                                    "NO CANDIDATE PACKS FOUND IN /sdcard/Download"
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) BrutalDarkGray else BrutalNeonGreen,
                    contentColor = BrutalBlack
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isScanning) "SCANNING..." else "SCAN & IMPORT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrutalBorder, RoundedCornerShape(8.dp))
                    .background(BrutalDarkSurface, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = statusMessage!!,
                    color = BrutalYellow,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Air-Gapped Instructions Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBorder, RoundedCornerShape(8.dp))
                .background(BrutalBlack, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "AIR-GAP SIDELOAD PROTOCOL (ZERO INTERNET PERMISSION):",
                    color = BrutalWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Download pack from huggingface.co/datasets/Pranav00x/commonmsm-packs via PC or phone browser.\n" +
                            "2. Place in /sdcard/Download/ or execute:\n" +
                            "   adb push <file> /sdcard/Download/\n" +
                            "3. Tap [SCAN & IMPORT] above to copy and verify checksum.",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 14.sp
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
                    .border(2.dp, BrutalBorder, RoundedCornerShape(10.dp))
                    .background(BrutalDarkSurface, RoundedCornerShape(10.dp))
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
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )

                        val badgeText: String
                        val badgeColor: androidx.compose.ui.graphics.Color
                        when {
                            isHashMatch == true -> {
                                badgeText = "SHA-256 OK"
                                badgeColor = BrutalNeonGreen
                            }
                            isHashMatch == false -> {
                                badgeText = "HASH MISMATCH"
                                badgeColor = BrutalRed
                            }
                            status.isInstalled -> {
                                badgeText = "INSTALLED (DISK)"
                                badgeColor = BrutalNeonGreen
                            }
                            pack.targetFileName.endsWith(".gguf") -> {
                                badgeText = "SIDELOAD PENDING"
                                badgeColor = BrutalGray
                            }
                            else -> {
                                badgeText = "BUILT-IN RAM (ACTIVE)"
                                badgeColor = BrutalYellow
                            }
                        }

                        BrutalCircularStamp(text = badgeText, color = badgeColor)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val sizeMb = pack.sizeBytes / (1024 * 1024)
                    val sizeDisplay = if (sizeMb >= 1024) String.format("%.2f GB", sizeMb.toDouble() / 1024.0) else "$sizeMb MB"
                    Text(
                        text = "FILE: ${pack.targetFileName} // SIZE: $sizeDisplay",
                        color = BrutalGray,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pack.description,
                        color = BrutalWhite,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "HF: ${pack.downloadUrl}",
                        color = BrutalBlue,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )

                    if (status.isInstalled && status.localFile != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXPECTED: ${pack.sha256Checksum.take(12)}...",
                                color = BrutalGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
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
                                    containerColor = if (isVerifying) BrutalDarkGray else BrutalOrange,
                                    contentColor = BrutalBlack
                                ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isVerifying) "HASHING..." else "VERIFY SHA-256",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "DETECTED GGUF ARCHITECTURES",
            color = BrutalGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (discoveredModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                    .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "No custom .gguf found in /sdcard/OfflineAI/. Using built-in local inference synthesizer.",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        } else {
            discoveredModels.forEach { model ->
                val inspectResult = remember(model.file.absolutePath) { GgufMetadataInspector.inspectFile(model.file) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                        .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
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
                                    text = model.name.uppercase(),
                                    color = BrutalWhite,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${String.format("%.2f", model.sizeBytes.toDouble() / (1024 * 1024 * 1024))} GB // ${if (model.isMoE) "MOE FLASH" else "DENSE RAM"}",
                                    color = BrutalGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { onSelectModel(model) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrutalOrange, contentColor = BrutalBlack),
                                shape = CircleShape
                            ) {
                                Text("SELECT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }

                        if (inspectResult.isValid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BrutalCircularStamp(text = "GGUF v${inspectResult.version}", color = BrutalNeonGreen)
                                BrutalCircularStamp(text = inspectResult.architecture.uppercase(), color = BrutalBlue)
                                BrutalCircularStamp(text = inspectResult.fileTypeDescription, color = BrutalYellow)
                                BrutalCircularStamp(
                                    text = if (inspectResult.fitsInRamBudget) "RAM OK" else "EXCEEDS 12GB",
                                    color = if (inspectResult.fitsInRamBudget) BrutalNeonGreen else BrutalRed
                                )
                            }
                        } else if (model.file.exists()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            BrutalCircularStamp(text = inspectResult.diagnosticMessage, color = BrutalRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStorageStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = BrutalGray, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = BrutalWhite, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

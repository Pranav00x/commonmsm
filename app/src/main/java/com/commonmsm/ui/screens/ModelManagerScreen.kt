package com.commonmsm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.DiscoveredModel
import com.commonmsm.data.StorageReport
import com.commonmsm.ui.theme.*

@Composable
fun ModelManagerScreen(
    storageReport: StorageReport,
    discoveredModels: List<DiscoveredModel>,
    isMoEActive: Boolean,
    onToggleMoE: (Boolean) -> Unit,
    onSelectModel: (DiscoveredModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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

        Text(
            text = "OFFLINE KNOWLEDGE STATUS",
            color = BrutalGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        BrutalKnowledgeRow(title = "PLACES.DB", detail = "21.1M POIs (OSM+Overture)", tag = "ONLINE")
        BrutalKnowledgeRow(title = "WIKI.DB", detail = "2.0M FineWiki Articles FTS5", tag = "ONLINE")
        BrutalKnowledgeRow(title = "CRYPTO.DB", detail = "1,208 EIPs / NIST PQC", tag = "ONLINE")

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                        .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = model.name.uppercase(),
                                color = BrutalWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${model.sizeBytes / (1024 * 1024 * 1024)} GB // ${if (model.isMoE) "MOE FLASH" else "DENSE RAM"}",
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

@Composable
private fun BrutalKnowledgeRow(title: String, detail: String, tag: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .border(2.dp, BrutalBorder, RoundedCornerShape(8.dp))
            .background(BrutalDarkSurface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, color = BrutalWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text(text = detail, color = BrutalGray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BrutalBlack)
                    .border(1.dp, BrutalNeonGreen, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = tag, color = BrutalNeonGreen, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

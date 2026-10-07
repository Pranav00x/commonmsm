package com.commonmsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.ExecutionStats
import com.commonmsm.ui.theme.*

@Composable
fun PerformanceHUD(
    stats: ExecutionStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
            .background(BrutalDarkSurface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TELEMETRY // ON-DEVICE EXECUTION",
                color = BrutalGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BrutalNeonGreen)
                    .size(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Circular Gauges in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularGaugeItem(
                label = "TPS",
                displayValue = String.format("%.1f", stats.tokensPerSecond),
                unit = "t/s",
                progress = (stats.tokensPerSecond / 40f).coerceIn(0.05f, 1f),
                indicatorColor = if (stats.tokensPerSecond >= 15f) BrutalNeonGreen else BrutalOrange
            )

            CircularGaugeItem(
                label = "TTFT",
                displayValue = "${stats.timeToFirstTokenMs}",
                unit = "ms",
                progress = (1f - (stats.timeToFirstTokenMs / 3000f)).coerceIn(0.1f, 1f),
                indicatorColor = BrutalBlue
            )

            CircularGaugeItem(
                label = "RAM",
                displayValue = "${stats.memoryUsedMb}",
                unit = "MB",
                progress = (stats.memoryUsedMb / 12000f).coerceIn(0.05f, 1f),
                indicatorColor = BrutalYellow
            )

            CircularGaugeItem(
                label = if (stats.moeCacheHitRate != null) "HIT" else "TOTAL",
                displayValue = if (stats.moeCacheHitRate != null)
                    "${(stats.moeCacheHitRate * 100).toInt()}%"
                else
                    String.format("%.1fs", stats.totalTimeMs / 1000f),
                unit = if (stats.moeCacheHitRate != null) "cache" else "sec",
                progress = if (stats.moeCacheHitRate != null)
                    stats.moeCacheHitRate.toFloat().coerceIn(0.1f, 1f)
                else
                    0.85f,
                indicatorColor = if (stats.moeCacheHitRate != null) BrutalNeonGreen else BrutalWhite
            )
        }
    }
}

@Composable
fun CircularGaugeItem(
    label: String,
    displayValue: String,
    unit: String,
    progress: Float,
    indicatorColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(BrutalBlack)
                .border(2.dp, BrutalBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress ring (solid color, zero gradient)
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(54.dp),
                color = indicatorColor,
                trackColor = BrutalDarkGray,
                strokeWidth = 4.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayValue,
                    color = BrutalWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                Text(
                    text = unit,
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = BrutalGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

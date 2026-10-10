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
            .clip(RoundedCornerShape(12.dp))
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "On-Device Execution Stats",
                color = DeepSeekTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (stats.deviceTempCelsius != null) {
                    Text(
                        text = "${stats.deviceTempCelsius.toInt()}°C",
                        color = if ((stats.deviceTempCelsius ?: 0f) > 42f) DeepSeekRed else DeepSeekGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if ((stats.deviceTempCelsius ?: 0f) > 42f) DeepSeekRed else DeepSeekGreen)
                        .size(7.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Modern Stat Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularGaugeItem(
                label = "Speed",
                displayValue = String.format("%.1f", stats.tokensPerSecond),
                unit = "t/s",
                progress = (stats.tokensPerSecond / 40f).coerceIn(0.05f, 1f),
                indicatorColor = if (stats.tokensPerSecond >= 15f) DeepSeekGreen else DeepSeekBlue
            )

            CircularGaugeItem(
                label = "First Token",
                displayValue = "${stats.timeToFirstTokenMs}",
                unit = "ms",
                progress = (1f - (stats.timeToFirstTokenMs / 3000f)).coerceIn(0.1f, 1f),
                indicatorColor = DeepSeekBlueLight
            )

            CircularGaugeItem(
                label = "Memory",
                displayValue = "${stats.memoryUsedMb}",
                unit = "MB",
                progress = (stats.memoryUsedMb / 12000f).coerceIn(0.05f, 1f),
                indicatorColor = DeepSeekAmber
            )

            CircularGaugeItem(
                label = if (stats.moeCacheHitRate != null) "Cache Hit" else "Latency",
                displayValue = if (stats.moeCacheHitRate != null)
                    "${(stats.moeCacheHitRate * 100).toInt()}%"
                else
                    String.format("%.1fs", stats.totalTimeMs / 1000f),
                unit = if (stats.moeCacheHitRate != null) "hit" else "total",
                progress = if (stats.moeCacheHitRate != null)
                    stats.moeCacheHitRate.toFloat().coerceIn(0.1f, 1f)
                else
                    0.85f,
                indicatorColor = if (stats.moeCacheHitRate != null) DeepSeekGreen else DeepSeekTextPrimary
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
                .size(56.dp)
                .clip(CircleShape)
                .background(DeepSeekCard)
                .border(1.dp, DeepSeekBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(46.dp),
                color = indicatorColor,
                trackColor = DeepSeekBorder,
                strokeWidth = 3.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayValue,
                    color = DeepSeekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = unit,
                    color = DeepSeekTextMuted,
                    fontSize = 8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = DeepSeekTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

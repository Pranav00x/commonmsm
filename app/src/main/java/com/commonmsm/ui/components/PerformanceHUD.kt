package com.commonmsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(label = "Speed", value = String.format("%.1f t/s", stats.tokensPerSecond), highlight = stats.tokensPerSecond >= 15f)
        StatItem(label = "TTFT", value = "${stats.timeToFirstTokenMs}ms")
        StatItem(label = "Total", value = String.format("%.1fs", stats.totalTimeMs / 1000f))
        StatItem(label = "RAM", value = "${stats.memoryUsedMb}MB")
        if (stats.moeCacheHitRate != null) {
            StatItem(label = "MoE Hit", value = String.format("%.0f%%", stats.moeCacheHitRate * 100))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = if (highlight) AccentGreen else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

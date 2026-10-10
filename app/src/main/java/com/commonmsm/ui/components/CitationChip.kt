package com.commonmsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.Citation
import com.commonmsm.ui.theme.*

@Composable
fun CitationChip(
    citation: Citation,
    onClick: (Citation) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
            .clickable { onClick(citation) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Number Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(DeepSeekBlue.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "[${citation.index}]",
                color = DeepSeekBlueLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = citation.title.take(28) + if (citation.title.length > 28) "..." else "",
            color = DeepSeekTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

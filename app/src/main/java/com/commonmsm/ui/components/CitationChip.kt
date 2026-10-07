package com.commonmsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
            .clip(CircleShape)
            .background(BrutalBlack)
            .border(2.dp, BrutalBorder, CircleShape)
            .clickable { onClick(citation) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Stark Circular Index Badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BrutalOrange)
                .border(1.dp, BrutalBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${citation.index}",
                color = BrutalBlack,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = citation.title.take(24) + if (citation.title.length > 24) "..." else "",
            color = BrutalWhite,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

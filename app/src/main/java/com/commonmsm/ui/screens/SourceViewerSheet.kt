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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.Citation
import com.commonmsm.ui.theme.*

@Composable
fun SourceViewerSheet(
    citation: Citation?,
    onDismiss: () -> Unit
) {
    if (citation == null) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = DeepSeekBg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(DeepSeekBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${citation.index}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Source: ${citation.source}",
                        color = DeepSeekTextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DeepSeekSurface)
                        .border(1.dp, DeepSeekBorder, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DeepSeekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = citation.title,
                color = DeepSeekTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DeepSeekBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepSeekSurface)
                    .border(1.dp, DeepSeekBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Grounded Source Passage",
                    color = DeepSeekBlueLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = citation.snippet,
                    color = DeepSeekTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

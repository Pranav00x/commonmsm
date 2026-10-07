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
            .border(2.dp, BrutalBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = BrutalBlack,
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BrutalOrange)
                            .border(1.dp, BrutalBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${citation.index}",
                            color = BrutalBlack,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SOURCE // ${citation.source.uppercase()}",
                        color = BrutalWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                // Circular Close Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrutalDarkSurface)
                        .border(1.5.dp, BrutalBorder, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = BrutalWhite, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = citation.title.uppercase(),
                color = BrutalOrange,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BrutalBorder, thickness = 2.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .border(2.dp, BrutalBorder, RoundedCornerShape(8.dp))
                    .background(BrutalDarkSurface, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "GROUND-TRUTH CORPUS PASSAGE:",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = citation.snippet,
                    color = BrutalWhite,
                    fontFamily = FontFamily.Default,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

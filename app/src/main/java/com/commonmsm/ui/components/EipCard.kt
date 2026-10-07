package com.commonmsm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.EipEntity
import com.commonmsm.ui.theme.*

@Composable
fun EipCard(
    spec: EipEntity,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
            .background(BrutalElevated, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row: EIP Number Pill + Upgrade Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Circular EIP Pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrutalBlack)
                            .border(2.dp, BrutalOrange, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EIP-${spec.eipNumber}",
                            color = BrutalOrange,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    // Status Stamp
                    BrutalCircularStamp(
                        text = spec.status.uppercase(),
                        color = if (spec.status.equals("Final", ignoreCase = true)) BrutalNeonGreen else BrutalYellow
                    )

                    // Upgrade Stamp if available
                    if (!spec.networkUpgrade.isNullOrBlank()) {
                        BrutalCircularStamp(
                            text = spec.networkUpgrade.uppercase(),
                            color = BrutalBlue
                        )
                    }
                }

                // Expand/Collapse Toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BrutalDarkSurface)
                        .border(1.5.dp, BrutalBorder, CircleShape)
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Spec",
                        tint = BrutalWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = spec.title,
                color = BrutalWhite,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Author
            Text(
                text = "AUTHORS: ${spec.author}",
                color = BrutalGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            Text(
                text = spec.summary,
                color = BrutalWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Collapsible Full Technical Spec Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, BrutalBorder, RoundedCornerShape(8.dp))
                            .background(BrutalBlack, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = "Spec",
                                    tint = BrutalNeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "FORMAL SPECIFICATION // VERIFIED:",
                                    color = BrutalNeonGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = spec.fullSpec,
                                color = BrutalWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

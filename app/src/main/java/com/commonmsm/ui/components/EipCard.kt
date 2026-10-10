package com.commonmsm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row: EIP Number Badge + Status + Expand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepSeekBlue.copy(alpha = 0.15f))
                            .border(1.dp, DeepSeekBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EIP-${spec.eipNumber}",
                            color = DeepSeekBlueLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    ModernBadge(
                        text = spec.status,
                        color = if (spec.status.equals("Final", ignoreCase = true)) DeepSeekGreen else DeepSeekAmber
                    )

                    if (!spec.networkUpgrade.isNullOrBlank()) {
                        ModernBadge(
                            text = spec.networkUpgrade,
                            color = DeepSeekBlueLight
                        )
                    }
                }

                // Expand/Collapse Toggle
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DeepSeekCard)
                        .border(1.dp, DeepSeekBorder, CircleShape)
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Spec",
                        tint = DeepSeekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = spec.title,
                color = DeepSeekTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Author
            Text(
                text = "Authors: ${spec.author}",
                color = DeepSeekTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = spec.summary,
                color = DeepSeekTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Cross-Spec Dependencies & Relations from EipKnowledgeGraph
            val relations = remember(spec.eipNumber) { com.commonmsm.engine.EipKnowledgeGraph.getRelations(spec.eipNumber) }
            if (relations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dependencies & Cross-References",
                    color = DeepSeekTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relations.forEach { rel ->
                        ModernBadge(
                            text = "${rel.relationship}: EIP-${rel.targetEip}",
                            color = when (rel.relationship) {
                                "SUPERSEDES", "SUPERSEDED_BY" -> DeepSeekAmber
                                "REQUIRES" -> DeepSeekRed
                                "EXTENDS" -> DeepSeekBlueLight
                                else -> DeepSeekBlue
                            }
                        )
                    }
                }
            }

            // Collapsible Full Technical Spec Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    SpecDependencyGraphView(eipNumber = spec.eipNumber)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepSeekCard)
                            .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
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
                                    tint = DeepSeekGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Technical Specification",
                                    color = DeepSeekGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = spec.fullSpec,
                                color = DeepSeekTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

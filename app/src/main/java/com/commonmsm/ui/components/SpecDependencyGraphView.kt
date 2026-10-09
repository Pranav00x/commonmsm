package com.commonmsm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.engine.EipKnowledgeGraph
import com.commonmsm.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpecDependencyGraphView(
    eipNumber: Int,
    modifier: Modifier = Modifier
) {
    val relations = EipKnowledgeGraph.getRelations(eipNumber)
    if (relations.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
            .background(BrutalBlack, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEC DEPENDENCY GRAPH // EIP-$eipNumber",
                    color = BrutalOrange,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${relations.size} EDGES ACTIVE",
                    color = BrutalNeonGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Directed Graph
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(1.5.dp, BrutalBorder, CircleShape)
                    .background(BrutalDarkSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 30f

                    // Draw edges from center to satellites
                    relations.forEachIndexed { i, rel ->
                        val angleDeg = (i.toFloat() / relations.size) * 360f - 90f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val targetX = center.x + (radius * cos(angleRad)).toFloat()
                        val targetY = center.y + (radius * sin(angleRad)).toFloat()

                        val edgeColor = when (rel.relationship) {
                            "SUPERSEDES" -> BrutalYellow
                            "REQUIRES" -> BrutalRed
                            "EXTENDS" -> BrutalBlue
                            else -> BrutalOrange
                        }

                        // Edge Line
                        drawLine(
                            color = edgeColor,
                            start = center,
                            end = Offset(targetX, targetY),
                            strokeWidth = 2.0f
                        )

                        // Satellite node outer
                        drawCircle(
                            color = BrutalBlack,
                            radius = 16f,
                            center = Offset(targetX, targetY)
                        )
                        // Satellite node border
                        drawCircle(
                            color = edgeColor,
                            radius = 16f,
                            center = Offset(targetX, targetY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }

                    // Center Root Node
                    drawCircle(
                        color = BrutalBlack,
                        radius = 24f,
                        center = center
                    )
                    drawCircle(
                        color = BrutalNeonGreen,
                        radius = 24f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                    )
                }

                // Center Label
                Text(
                    text = "$eipNumber",
                    color = BrutalNeonGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graph Edge Breakdown
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                relations.forEach { rel ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "-> ${rel.relationship} EIP-${rel.targetEip}",
                            color = when (rel.relationship) {
                                "SUPERSEDES" -> BrutalYellow
                                "REQUIRES" -> BrutalRed
                                "EXTENDS" -> BrutalBlue
                                else -> BrutalOrange
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                        Text(
                            text = rel.summary.take(38) + "...",
                            color = BrutalGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

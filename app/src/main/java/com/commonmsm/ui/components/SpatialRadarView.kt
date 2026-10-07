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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.PlaceEntity
import com.commonmsm.engine.SpatialMath
import com.commonmsm.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpatialRadarView(
    places: List<PlaceEntity>,
    userLat: Double = 38.7100, // Default Lisbon center or current GNSS
    userLon: Double = -9.1380,
    modifier: Modifier = Modifier
) {
    if (places.isEmpty()) return

    val maxDistMeters = places.maxOfOrNull {
        it.distanceMeters?.toDouble() ?: SpatialMath.haversineDistanceMeters(userLat, userLon, it.latitude, it.longitude)
    }?.coerceAtLeast(1000.0) ?: 2000.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
            .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TACTICAL RADAR // GNSS POI SCOPE",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${places.size} TARGETS // ${SpatialMath.formatDistance(maxDistMeters)}",
                    color = BrutalNeonGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Radar Scope Canvas
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, BrutalBorder, CircleShape)
                    .background(BrutalBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(230.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f - 10f

                    // 1. Concentric Range Rings
                    val ringSteps = listOf(0.33f, 0.66f, 1.0f)
                    ringSteps.forEach { fraction ->
                        drawCircle(
                            color = BrutalBorder,
                            radius = maxRadius * fraction,
                            center = center,
                            style = Stroke(width = 1.5f)
                        )
                    }

                    // 2. Crosshair Axes
                    drawLine(
                        color = BrutalBorder,
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = BrutalBorder,
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.5f
                    )

                    // 3. Center Origin (User Location)
                    drawCircle(
                        color = BrutalWhite,
                        radius = 4f,
                        center = center
                    )

                    // 4. Plot Venue Target Blips
                    places.take(8).forEach { place ->
                        val dist = place.distanceMeters?.toDouble()
                            ?: SpatialMath.haversineDistanceMeters(userLat, userLon, place.latitude, place.longitude)
                        val bearingDeg = SpatialMath.calculateBearing(userLat, userLon, place.latitude, place.longitude)

                        val normalizedDist = (dist / maxDistMeters).coerceIn(0.05, 1.0).toFloat()
                        val r = maxRadius * normalizedDist

                        // Angle in math coordinates (0 deg North = -90 deg in canvas radians)
                        val angleRad = Math.toRadians(bearingDeg - 90.0)
                        val blipX = center.x + (r * cos(angleRad)).toFloat()
                        val blipY = center.y + (r * sin(angleRad)).toFloat()

                        val blipColor = if (place.isStrictlyVegan) BrutalNeonGreen else BrutalOrange

                        // Outer ring of blip
                        drawCircle(
                            color = blipColor,
                            radius = 6f,
                            center = Offset(blipX, blipY)
                        )
                        // Inner dot
                        drawCircle(
                            color = BrutalBlack,
                            radius = 2.5f,
                            center = Offset(blipX, blipY)
                        )
                    }
                }

                // Cardinal Labels on Radar Rim
                Text(
                    text = "N",
                    color = BrutalNeonGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                )
                Text(
                    text = "S",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                )
                Text(
                    text = "W",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp)
                )
                Text(
                    text = "E",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Radar Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(BrutalNeonGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "100% VEGAN", color = BrutalWhite, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(BrutalOrange, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "VEGAN OPTIONS", color = BrutalWhite, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(BrutalWhite, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "YOU (GNSS)", color = BrutalWhite, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

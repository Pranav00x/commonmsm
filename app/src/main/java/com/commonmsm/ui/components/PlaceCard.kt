package com.commonmsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
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
import com.commonmsm.data.models.PlaceEntity
import com.commonmsm.ui.theme.*

@Composable
fun PlaceCard(
    place: PlaceEntity,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
            .background(BrutalElevated, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = BrutalWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${place.city.uppercase()}, ${place.country.uppercase()}",
                        color = BrutalGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Stark Circular Score Badge
                if (place.fameScore > 0) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrutalBlack)
                            .border(2.dp, BrutalYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%.1f", place.fameScore),
                            color = BrutalYellow,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Circular Badges & Diet Stamps
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (place.isStrictlyVegan) {
                    BrutalCircularStamp(text = "VEGAN: 100%", color = BrutalNeonGreen)
                } else if (place.dietTags.contains("vegan")) {
                    BrutalCircularStamp(text = "VEGAN OPTIONS", color = BrutalNeonGreen)
                }

                if (!place.cuisine.isNullOrBlank()) {
                    BrutalCircularStamp(text = place.cuisine.uppercase(), color = BrutalGray)
                }

                if (place.distanceMeters != null) {
                    BrutalCircularStamp(text = com.commonmsm.engine.SpatialMath.formatDistance(place.distanceMeters.toDouble()), color = BrutalBlue)
                }
            }

            if (!place.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = BrutalOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = place.address,
                        color = BrutalWhite,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (!place.openingHours.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Hours",
                        tint = BrutalGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HRS: ${place.openingHours}",
                        color = BrutalGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: GPS coordinates & Navigate button
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPS: [${String.format("%.4f", place.latitude)}, ${String.format("%.4f", place.longitude)}]",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BrutalBlack)
                        .border(1.5.dp, BrutalOrange, CircleShape)
                        .androidx.compose.foundation.clickable {
                            val uri = android.net.Uri.parse("geo:${place.latitude},${place.longitude}?q=${android.net.Uri.encode(place.name)}")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback if no map handler installed
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NAVIGATE ->",
                        color = BrutalOrange,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun BrutalCircularStamp(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(BrutalBlack)
            .border(1.5.dp, color, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

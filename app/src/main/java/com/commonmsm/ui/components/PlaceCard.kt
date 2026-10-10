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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.PlaceEntity
import com.commonmsm.engine.OfflineGlossaryEngine
import com.commonmsm.ui.theme.*

@Composable
fun PlaceCard(
    place: PlaceEntity,
    onClick: () -> Unit = {}
) {
    var showDietCard by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
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
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepSeekTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${place.city}, ${place.country}",
                        color = DeepSeekTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Rating Badge
                if (place.fameScore > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepSeekAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = DeepSeekAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", place.fameScore),
                            color = DeepSeekAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dietary & Cuisine Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                if (place.isStrictlyVegan) {
                    ModernBadge(text = "Dedicated Vegan", color = DeepSeekGreen)
                } else if (place.dietTags.contains("vegan")) {
                    ModernBadge(text = "Vegan Options", color = DeepSeekGreen)
                }

                if (!place.cuisine.isNullOrBlank()) {
                    ModernBadge(text = place.cuisine, color = DeepSeekTextSecondary)
                }

                if (place.distanceMeters != null) {
                    ModernBadge(
                        text = com.commonmsm.engine.SpatialMath.formatDistance(place.distanceMeters.toDouble()),
                        color = DeepSeekBlueLight
                    )
                }
            }

            if (!place.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = DeepSeekBlueLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = place.address,
                        color = DeepSeekTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (!place.openingHours.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Hours",
                        tint = DeepSeekTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = place.openingHours,
                        color = DeepSeekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.4f", place.latitude)}, ${String.format("%.4f", place.longitude)}",
                    color = DeepSeekTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val dietCard = remember(place.country) { OfflineGlossaryEngine.getDietaryCard(place.country) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (showDietCard) DeepSeekGreen.copy(alpha = 0.2f) else DeepSeekCard)
                            .border(1.dp, if (showDietCard) DeepSeekGreen else DeepSeekBorder, RoundedCornerShape(6.dp))
                            .clickable { showDietCard = !showDietCard }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showDietCard) "Hide Phrase" else "Diet Phrase (${dietCard.languageCode.uppercase()})",
                            color = if (showDietCard) DeepSeekGreen else DeepSeekTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepSeekBlue.copy(alpha = 0.15f))
                            .border(1.dp, DeepSeekBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable {
                                val uri = android.net.Uri.parse("geo:${place.latitude},${place.longitude}?q=${android.net.Uri.encode(place.name)}")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Navigate",
                            color = DeepSeekBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showDietCard) {
                val dietCard = remember(place.country) { OfflineGlossaryEngine.getDietaryCard(place.country) }
                val clipboardManager = LocalClipboardManager.current
                var copied by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepSeekCard)
                        .border(1.dp, DeepSeekBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dietary Phrase: ${dietCard.languageName}",
                            color = DeepSeekGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DeepSeekSurface)
                                .border(1.dp, DeepSeekBorder, RoundedCornerShape(4.dp))
                                .clickable {
                                    val fullPhrase = "${dietCard.headlinePhrase}\n${dietCard.detailedExplanation}"
                                    clipboardManager.setText(AnnotatedString(fullPhrase))
                                    copied = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (copied) "Copied" else "Copy",
                                color = DeepSeekTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dietCard.headlinePhrase,
                        color = DeepSeekTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dietCard.detailedExplanation,
                        color = DeepSeekTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BrutalCircularStamp(text: String, color: Color) {
    ModernBadge(text = text, color = color)
}

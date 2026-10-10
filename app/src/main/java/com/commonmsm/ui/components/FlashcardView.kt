package com.commonmsm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.engine.Flashcard
import com.commonmsm.engine.ResearchFlashcardEngine
import com.commonmsm.ui.theme.*

@Composable
fun FlashcardView(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cards by remember { mutableStateOf(ResearchFlashcardEngine.getDueCards().ifEmpty { ResearchFlashcardEngine.getAllCards() }) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf(ResearchFlashcardEngine.getDeckStats()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = DeepSeekBg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DeepSeekGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spaced Repetition Flashcards",
                        color = DeepSeekTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
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
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DeepSeekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DeepSeekBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Stats Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernBadge(text = "Total: ${stats.totalCount}", color = DeepSeekTextSecondary)
                ModernBadge(text = "Due: ${stats.dueCount}", color = DeepSeekGreen)
                ModernBadge(text = "Mastered: ${stats.masteredCount}", color = DeepSeekBlueLight)
                ModernBadge(text = "EF: ${String.format("%.2f", stats.averageEaseFactor)}", color = DeepSeekAmber)
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepSeekSurface)
                        .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No flashcards in queue",
                            color = DeepSeekTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                ResearchFlashcardEngine.resetDeck()
                                cards = ResearchFlashcardEngine.getAllCards()
                                currentIndex = 0
                                stats = ResearchFlashcardEngine.getDeckStats()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepSeekBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reload Preset Deck", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                val currentCard = cards[currentIndex.coerceIn(0, cards.size - 1)]

                // Main Flashcard Frame
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepSeekSurface)
                        .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ModernBadge(text = currentCard.category, color = DeepSeekBlueLight)
                            Text(
                                text = "Card ${currentIndex + 1} of ${cards.size}",
                                color = DeepSeekTextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Interval: ${currentCard.intervalDays}d • Repetitions: ${currentCard.repetitions} • EF: ${String.format("%.2f", currentCard.easeFactor)}",
                            color = DeepSeekTextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Question",
                            color = DeepSeekBlueLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentCard.question,
                            color = DeepSeekTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Answer Block
                        AnimatedVisibility(visible = isRevealed) {
                            Column {
                                HorizontalDivider(color = DeepSeekBorder, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Answer",
                                    color = DeepSeekGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentCard.answer,
                                    color = DeepSeekTextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Reveal Button or Grading Buttons
                    if (!isRevealed) {
                        Button(
                            onClick = { isRevealed = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepSeekBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reveal Answer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Column {
                            Text(
                                text = "Rate Retention (SM-2)",
                                color = DeepSeekTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GradeButton(
                                    label = "Again",
                                    color = DeepSeekRed,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ResearchFlashcardEngine.gradeCard(currentCard, 1)
                                    stats = ResearchFlashcardEngine.getDeckStats()
                                    isRevealed = false
                                    currentIndex = (currentIndex + 1) % cards.size
                                }

                                GradeButton(
                                    label = "Hard",
                                    color = DeepSeekAmber,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ResearchFlashcardEngine.gradeCard(currentCard, 3)
                                    stats = ResearchFlashcardEngine.getDeckStats()
                                    isRevealed = false
                                    currentIndex = (currentIndex + 1) % cards.size
                                }

                                GradeButton(
                                    label = "Good",
                                    color = DeepSeekBlue,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ResearchFlashcardEngine.gradeCard(currentCard, 4)
                                    stats = ResearchFlashcardEngine.getDeckStats()
                                    isRevealed = false
                                    currentIndex = (currentIndex + 1) % cards.size
                                }

                                GradeButton(
                                    label = "Easy",
                                    color = DeepSeekGreen,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ResearchFlashcardEngine.gradeCard(currentCard, 5)
                                    stats = ResearchFlashcardEngine.getDeckStats()
                                    isRevealed = false
                                    currentIndex = (currentIndex + 1) % cards.size
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (currentIndex > 0) {
                                currentIndex--
                                isRevealed = false
                            }
                        },
                        enabled = currentIndex > 0
                    ) {
                        Text("Previous", color = if (currentIndex > 0) DeepSeekTextPrimary else DeepSeekTextMuted)
                    }

                    TextButton(
                        onClick = {
                            ResearchFlashcardEngine.resetDeck()
                            cards = ResearchFlashcardEngine.getAllCards()
                            currentIndex = 0
                            isRevealed = false
                            stats = ResearchFlashcardEngine.getDeckStats()
                        }
                    ) {
                        Text("Reset Deck", color = DeepSeekTextMuted, fontSize = 11.sp)
                    }

                    TextButton(
                        onClick = {
                            if (currentIndex < cards.size - 1) {
                                currentIndex++
                                isRevealed = false
                            }
                        },
                        enabled = currentIndex < cards.size - 1
                    ) {
                        Text("Next", color = if (currentIndex < cards.size - 1) DeepSeekTextPrimary else DeepSeekTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

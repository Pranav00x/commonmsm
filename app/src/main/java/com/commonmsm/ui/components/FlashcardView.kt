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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            .border(2.dp, BrutalBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = BrutalBlack,
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BrutalNeonGreen)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SM-2 TACTICAL FLASHCARDS",
                        color = BrutalWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrutalDarkSurface)
                        .border(1.5.dp, BrutalBorder, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BrutalWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BrutalBorder, thickness = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Stats Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrutalCircularStamp(text = "TOTAL: ${stats.totalCount}", color = BrutalWhite)
                BrutalCircularStamp(text = "DUE: ${stats.dueCount}", color = BrutalNeonGreen)
                BrutalCircularStamp(text = "MASTERED: ${stats.masteredCount}", color = BrutalBlue)
                BrutalCircularStamp(text = "EF: ${String.format("%.2f", stats.averageEaseFactor)}", color = BrutalYellow)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                        .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO FLASHCARDS IN QUEUE",
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(BrutalBlack)
                                .border(1.5.dp, BrutalNeonGreen, CircleShape)
                                .clickable {
                                    ResearchFlashcardEngine.resetDeck()
                                    cards = ResearchFlashcardEngine.getAllCards()
                                    currentIndex = 0
                                    stats = ResearchFlashcardEngine.getDeckStats()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "RELOAD PRESET DECK",
                                color = BrutalNeonGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
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
                        .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                        .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Card Metadata Header
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BrutalCircularStamp(text = currentCard.category, color = BrutalOrange)
                            Text(
                                text = "CARD ${currentIndex + 1} / ${cards.size}",
                                color = BrutalGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "INTERVAL: ${currentCard.intervalDays}D  •  REPS: ${currentCard.repetitions}  •  EF: ${String.format("%.2f", currentCard.easeFactor)}",
                            color = BrutalGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question Block
                        Text(
                            text = "QUESTION:",
                            color = BrutalNeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentCard.question,
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Answer Block (Animated)
                        AnimatedVisibility(visible = isRevealed) {
                            Column {
                                HorizontalDivider(color = BrutalBorder, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ANSWER & SPECIFICATION:",
                                    color = BrutalBlue,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentCard.answer,
                                    color = BrutalWhite,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Reveal Toggle Button
                    if (!isRevealed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrutalBlack)
                                .border(1.5.dp, BrutalWhite, RoundedCornerShape(8.dp))
                                .clickable { isRevealed = true }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "REVEAL ANSWER ->",
                                color = BrutalWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        // SM-2 Quality Score Evaluation Matrix
                        Column {
                            Text(
                                text = "SM-2 RETENTION SCORE:",
                                color = BrutalGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // AGAIN (Score 1)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(BrutalBlack)
                                        .border(1.5.dp, BrutalRed, CircleShape)
                                        .clickable {
                                            ResearchFlashcardEngine.gradeCard(currentCard, 1)
                                            stats = ResearchFlashcardEngine.getDeckStats()
                                            isRevealed = false
                                            currentIndex = (currentIndex + 1) % cards.size
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "AGAIN (1)",
                                        color = BrutalRed,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // HARD (Score 3)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(BrutalBlack)
                                        .border(1.5.dp, BrutalYellow, CircleShape)
                                        .clickable {
                                            ResearchFlashcardEngine.gradeCard(currentCard, 3)
                                            stats = ResearchFlashcardEngine.getDeckStats()
                                            isRevealed = false
                                            currentIndex = (currentIndex + 1) % cards.size
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "HARD (3)",
                                        color = BrutalYellow,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // GOOD (Score 4)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(BrutalBlack)
                                        .border(1.5.dp, BrutalBlue, CircleShape)
                                        .clickable {
                                            ResearchFlashcardEngine.gradeCard(currentCard, 4)
                                            stats = ResearchFlashcardEngine.getDeckStats()
                                            isRevealed = false
                                            currentIndex = (currentIndex + 1) % cards.size
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "GOOD (4)",
                                        color = BrutalBlue,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // EASY (Score 5)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(BrutalBlack)
                                        .border(1.5.dp, BrutalNeonGreen, CircleShape)
                                        .clickable {
                                            ResearchFlashcardEngine.gradeCard(currentCard, 5)
                                            stats = ResearchFlashcardEngine.getDeckStats()
                                            isRevealed = false
                                            currentIndex = (currentIndex + 1) % cards.size
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "EASY (5)",
                                        color = BrutalNeonGreen,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Deck Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrutalDarkSurface)
                            .border(1.5.dp, BrutalBorder, CircleShape)
                            .clickable {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    isRevealed = false
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "<- PREV",
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrutalDarkSurface)
                            .border(1.5.dp, BrutalBorder, CircleShape)
                            .clickable {
                                ResearchFlashcardEngine.resetDeck()
                                cards = ResearchFlashcardEngine.getAllCards()
                                currentIndex = 0
                                isRevealed = false
                                stats = ResearchFlashcardEngine.getDeckStats()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "RESET PRESETS",
                            color = BrutalGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrutalDarkSurface)
                            .border(1.5.dp, BrutalBorder, CircleShape)
                            .clickable {
                                if (currentIndex < cards.size - 1) {
                                    currentIndex++
                                    isRevealed = false
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "NEXT ->",
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

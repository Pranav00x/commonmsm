package com.commonmsm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.Citation
import com.commonmsm.engine.InferenceController
import com.commonmsm.ui.components.CitationChip
import com.commonmsm.ui.components.MarkdownRenderer
import com.commonmsm.ui.components.PerformanceHUD
import com.commonmsm.ui.components.PlaceCard
import com.commonmsm.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val update: InferenceController.StreamUpdate? = null
)

@Composable
fun ChatScreen(
    inferenceController: InferenceController,
    onOpenSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    var selectedCitation by remember { mutableStateOf<Citation?>(null) }

    fun sendQuery(queryText: String) {
        if (queryText.isBlank() || isGenerating) return
        val userMsgId = System.currentTimeMillis().toString()
        messages.add(ChatMessage(id = userMsgId, isUser = true, text = queryText))
        inputText = ""
        isGenerating = true

        val assistantMsgId = (System.currentTimeMillis() + 1).toString()
        val assistantMsg = ChatMessage(id = assistantMsgId, isUser = false, text = "")
        messages.add(assistantMsg)

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            inferenceController.executeResearchStream(queryText).collect { update ->
                val idx = messages.indexOfFirst { it.id == assistantMsgId }
                if (idx != -1) {
                    messages[idx] = assistantMsg.copy(
                        text = update.partialText,
                        update = update
                    )
                }
                listState.scrollToItem(messages.size - 1)
                if (update.isComplete) {
                    isGenerating = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalBlack)
                    .border(width = 0.dp, color = BrutalBlack)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "COMMONMSM",
                            color = BrutalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        // Circular Status Pill (No Gradients, Solid Border)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(BrutalBlack)
                                .border(1.5.dp, BrutalNeonGreen, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BrutalNeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFLINE",
                                color = BrutalNeonGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Circular Action Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrutalDarkSurface)
                            .border(2.dp, BrutalBorder, CircleShape)
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Config",
                            tint = BrutalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalBlack)
                    .padding(12.dp)
            ) {
                // Brutalist Circular Quick Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularBrutalChip("LISBON VEGAN") { sendQuery("Tell me the best vegan restaurants in Lisbon") }
                    CircularBrutalChip("EIP-7702 // 4337") { sendQuery("Compare EIP-7702 and ERC-4337 for account abstraction") }
                    CircularBrutalChip("FALCON VS ML-DSA") { sendQuery("Compare Falcon and ML-DSA post-quantum signature schemes for Ethereum") }
                    CircularBrutalChip("BERLIN VEGAN") { sendQuery("What are the best vegan restaurants in Berlin?") }
                }

                // Circular Pill Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(BrutalDarkSurface)
                            .border(2.dp, if (inputText.isNotBlank()) BrutalOrange else BrutalBorder, CircleShape)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "QUERY_LOCAL_CORPUS...",
                                    color = BrutalGray,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = BrutalWhite,
                                unfocusedTextColor = BrutalWhite,
                                cursorColor = BrutalOrange,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { sendQuery(inputText) }),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Solid Circular Send Button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isGenerating) BrutalOrange else BrutalDarkSurface)
                            .border(2.dp, if (inputText.isNotBlank() && !isGenerating) BrutalWhite else BrutalBorder, CircleShape)
                            .clickable(enabled = inputText.isNotBlank() && !isGenerating) { sendQuery(inputText) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Run",
                            tint = if (inputText.isNotBlank() && !isGenerating) BrutalBlack else BrutalGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrutalBlack)
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                // Brutalist Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Hero Core
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(BrutalDarkSurface)
                            .border(3.dp, BrutalOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MSM",
                            color = BrutalOrange,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "COMMONMSM // EDGE ENGINE",
                        color = BrutalWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "100% AIR-GAPPED HARDWARE SYNTHESIS",
                        color = BrutalGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Industrial Specs Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
                            .background(BrutalDarkSurface, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            BrutalSpecRow(tag = "CORPUS_PLACES", value = "21.1M POIs (OSM+OVERTURE)")
                            BrutalSpecRow(tag = "CORPUS_WIKI", value = "2.0M FINEWIKI FTS5")
                            BrutalSpecRow(tag = "CORPUS_SPECS", value = "1,208 EIPs / NIST PQC")
                            BrutalSpecRow(tag = "NETWORK_PERMISSION", value = "NONE (KERNEL ENFORCED)")
                            BrutalSpecRow(tag = "MAX_STORAGE", value = "50.0 GB HARD LIMIT")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { msg ->
                        if (msg.isUser) {
                            UserBrutalBubble(msg.text)
                        } else {
                            AssistantBrutalCard(
                                msg = msg,
                                onCitationClick = { selectedCitation = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            if (selectedCitation != null) {
                SourceViewerSheet(
                    citation = selectedCitation,
                    onDismiss = { selectedCitation = null }
                )
            }
        }
    }
}

@Composable
private fun CircularBrutalChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(BrutalDarkSurface)
            .border(1.5.dp, BrutalBorder, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = BrutalWhite,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BrutalSpecRow(tag: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "• $tag", color = BrutalGray, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = BrutalWhite, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun UserBrutalBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
                .background(BrutalDarkSurface)
                .border(2.dp, BrutalOrange, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "USER_INPUT >>",
                    color = BrutalOrange,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    color = BrutalWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AssistantBrutalCard(
    msg: ChatMessage,
    onCitationClick: (Citation) -> Unit
) {
    val update = msg.update

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BrutalDarkSurface)
            .border(2.dp, BrutalBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RESPONSE // GROUNDED ENGINE",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                if (update?.isComplete == true) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrutalNeonGreen)
                            .size(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Instant Verified Places Section
            if (update != null && update.instantPlaces.isNotEmpty()) {
                Text(
                    text = "🗺️ INSTANT_POI_MATCHES (${update.instantPlaces.size} FOUND)",
                    color = BrutalOrange,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                update.instantPlaces.take(3).forEach { place ->
                    PlaceCard(place = place)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Synthesized Body Text
            if (msg.text.isNotBlank()) {
                MarkdownRenderer(text = msg.text)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BrutalOrange,
                        trackColor = BrutalDarkGray,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SYNTHESIZING_GROUNDED_EVIDENCE...",
                        color = BrutalGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Verified Citation Pills
            if (update != null && update.report != null && update.report.citations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "VERIFIED CITATIONS:",
                    color = BrutalGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 6.dp)
                ) {
                    update.report.citations.forEach { citation ->
                        CitationChip(citation = citation, onClick = onCitationClick)
                    }
                }
            }

            // Brutalist Circular Performance HUD
            if (update != null && update.report != null) {
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceHUD(stats = update.report.stats)
            }
        }
    }
}

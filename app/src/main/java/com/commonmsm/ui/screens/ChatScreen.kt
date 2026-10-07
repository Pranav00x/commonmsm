package com.commonmsm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.Citation
import com.commonmsm.data.models.ResearchReport
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
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "commonmsm",
                            style = MaterialTheme.typography.titleLarge,
                            color = CommonMsmOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "Offline",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% OFFLINE",
                                    color = AccentGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickChip("🌿 Vegan in Lisbon") { sendQuery("Tell me the best vegan restaurants in Lisbon") }
                    QuickChip("⚙️ EIP-7702 vs ERC-4337") { sendQuery("Compare EIP-7702 and ERC-4337 for account abstraction") }
                    QuickChip("🛡️ Falcon vs ML-DSA") { sendQuery("Compare Falcon and ML-DSA post-quantum signature schemes for Ethereum") }
                    QuickChip("🇩🇪 Vegan in Berlin") { sendQuery("What are the best vegan restaurants in Berlin?") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask any research or travel query...", color = TextTertiary, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CommonMsmOrange,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendQuery(inputText) },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (inputText.isNotBlank() && !isGenerating) CommonMsmOrange else DarkSurfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isGenerating) DarkBackground else TextTertiary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "commonmsm",
                        style = MaterialTheme.typography.titleLarge,
                        color = CommonMsmOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Offline Information Lookup & Research Engine for Android",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "• Operates entirely without internet or Google Play Services\n• 21M+ Places (OSM + Overture) with dietary tags\n• Full Wikipedia FTS5 Index & Ethereum/Crypto Specs\n• Extreme MoE weight streaming from flash storage",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
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
                            UserMessageBubble(msg.text)
                        } else {
                            AssistantMessageCard(
                                msg = msg,
                                onCitationClick = { selectedCitation = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
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
private fun QuickChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                .background(CommonMsmDarkOrange)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = text, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun AssistantMessageCard(
    msg: ChatMessage,
    onCitationClick: (Citation) -> Unit
) {
    val update = msg.update

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (update != null && update.instantPlaces.isNotEmpty()) {
                Text(
                    text = "🗺️ Instant Verified Places (${update.instantPlaces.size} found in 42ms)",
                    color = CommonMsmAmber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                update.instantPlaces.take(3).forEach { place ->
                    PlaceCard(place = place)
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (msg.text.isNotBlank()) {
                MarkdownRenderer(text = msg.text)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = CommonMsmOrange,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Searching offline knowledge bases & synthesizing...",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            if (update != null && update.report != null && update.report.citations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "VERIFIED SOURCES:", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp)
                ) {
                    update.report.citations.forEach { citation ->
                        CitationChip(citation = citation, onClick = onCitationClick)
                    }
                }
            }

            if (update != null && update.report != null) {
                Spacer(modifier = Modifier.height(12.dp))
                PerformanceHUD(stats = update.report.stats)
            }
        }
    }
}

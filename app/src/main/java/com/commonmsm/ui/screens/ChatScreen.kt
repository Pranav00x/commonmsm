package com.commonmsm.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.DatabaseManager
import com.commonmsm.data.models.Citation
import com.commonmsm.data.models.ResearchSession
import com.commonmsm.engine.AirGapExporter
import com.commonmsm.engine.InferenceController
import com.commonmsm.engine.QuerySuggestEngine
import com.commonmsm.engine.ResearchFlashcardEngine
import com.commonmsm.ui.components.CitationChip
import com.commonmsm.ui.components.EipCard
import com.commonmsm.ui.components.FlashcardView
import com.commonmsm.ui.components.MarkdownRenderer
import com.commonmsm.ui.components.PerformanceHUD
import com.commonmsm.ui.components.PlaceCard
import com.commonmsm.ui.components.SpatialRadarView
import com.commonmsm.ui.screens.NotebookSheet
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
    var showNotebook by remember { mutableStateOf(false) }
    var showFlashcards by remember { mutableStateOf(false) }
    var savedSessions by remember { mutableStateOf<List<ResearchSession>>(emptyList()) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

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
                    .background(DeepSeekBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // DeepSeek Style Logo Avatar
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DeepSeekBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "CommonMSM",
                            color = DeepSeekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.3).sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Offline Status Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepSeekGreen.copy(alpha = 0.12f))
                                .border(1.dp, DeepSeekGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DeepSeekGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Offline",
                                color = DeepSeekGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Engine Mode Switcher Pill
                        var isMoEMode by remember { mutableStateOf(inferenceController.isMoEActive()) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepSeekSurface)
                                .border(1.dp, DeepSeekBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    isMoEMode = !isMoEMode
                                    inferenceController.setMoEActive(isMoEMode)
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isMoEMode) "DeepMoE" else "Fast 3B",
                                color = if (isMoEMode) DeepSeekBlueLight else DeepSeekTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Top Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButtonSmall(
                            text = "SM2",
                            onClick = { showFlashcards = true }
                        )

                        IconButtonIcon(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "History",
                            onClick = {
                                savedSessions = DatabaseManager.getRecentSessions(30)
                                showNotebook = true
                            }
                        )

                        IconButtonIcon(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            onClick = { onOpenSettings() }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepSeekBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick Suggestion Chips (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModernSuggestionPill("CRISPR vs Prime Editing") { sendQuery("Compare CRISPR-Cas9 and Prime Editing for targeted genetic modification") }
                    ModernSuggestionPill("Roman Concrete") { sendQuery("Why did ancient Roman maritime concrete exhibit greater longevity in seawater than modern Portland cement?") }
                    ModernSuggestionPill("Late Bronze Age Collapse") { sendQuery("What were the primary hypotheses explaining the Late Bronze Age Collapse around 1200 BCE?") }
                    ModernSuggestionPill("Lisbon Vegan Cafes") { sendQuery("Tell me the best vegan restaurants in Lisbon") }
                    ModernSuggestionPill("EIP-7702 vs ERC-4337") { sendQuery("Compare EIP-7702 and ERC-4337 for account abstraction") }
                    ModernSuggestionPill("Falcon vs ML-DSA") { sendQuery("Compare Falcon and ML-DSA post-quantum signature schemes for Ethereum") }
                    ModernSuggestionPill("EIP-4844 Blobs") { sendQuery("How does EIP-4844 reduce Layer 2 rollup transaction costs?") }
                    ModernSuggestionPill("Near Me (Offline GNSS)") { sendQuery("Find the best vegan places near me using offline GNSS") }
                }

                // Autocomplete Suggestions
                val suggestions = remember(inputText) { QuerySuggestEngine.getSuggestions(inputText) }
                if (suggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { sugg ->
                            ModernSuggestionPill(sugg) {
                                inputText = ""
                                sendQuery(sugg)
                            }
                        }
                    }
                }

                // DeepSeek Capsule Input Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DeepSeekCard)
                        .border(1.dp, if (inputText.isNotBlank()) DeepSeekBlue.copy(alpha = 0.6f) else DeepSeekBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DeepSearch Indicator Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepSeekBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DeepSearch",
                                color = DeepSeekBlueLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Field
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Ask a research question...",
                                    color = DeepSeekTextMuted,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = DeepSeekTextPrimary,
                                unfocusedTextColor = DeepSeekTextPrimary,
                                cursorColor = DeepSeekBlue,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { sendQuery(inputText) }),
                            singleLine = true
                        )

                        // Voice Mic Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask offline research query...")
                                    }
                                    try {
                                        speechLauncher.launch(intent)
                                    } catch (_: Exception) {}
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = DeepSeekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Circular Send Button
                        val canSend = inputText.isNotBlank() && !isGenerating
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (canSend) DeepSeekBlue else DeepSeekBorder)
                                .clickable(enabled = canSend) { sendQuery(inputText) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else DeepSeekTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSeekBg)
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                // DeepSeek Style Welcoming Hero Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(DeepSeekBlue.copy(alpha = 0.15f))
                            .border(1.5.dp, DeepSeekBlue.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = DeepSeekBlueLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CommonMSM",
                        color = DeepSeekTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Offline Information & Research Engine",
                        color = DeepSeekTextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4 Clean Prompt Cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroPromptCard(
                            title = "Compare CRISPR-Cas9 and Prime Editing",
                            subtitle = "Genetics & Molecular Biology",
                            onClick = { sendQuery("Compare CRISPR-Cas9 and Prime Editing for targeted genetic modification") }
                        )

                        HeroPromptCard(
                            title = "Why Roman concrete lasted in seawater",
                            subtitle = "Material Science & Ancient History",
                            onClick = { sendQuery("Why did ancient Roman maritime concrete exhibit greater longevity in seawater than modern Portland cement?") }
                        )

                        HeroPromptCard(
                            title = "Compare EIP-7702 and ERC-4337",
                            subtitle = "Ethereum Protocol Specifications",
                            onClick = { sendQuery("Compare EIP-7702 and ERC-4337 for account abstraction") }
                        )

                        HeroPromptCard(
                            title = "Find vegan restaurants in Lisbon",
                            subtitle = "Local Venue Discovery & Dietary Tags",
                            onClick = { sendQuery("Tell me the best vegan restaurants in Lisbon") }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "100% Air-Gapped • No Internet Permission Declared",
                        color = DeepSeekTextMuted,
                        fontSize = 11.sp
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
                                onCitationClick = { selectedCitation = it },
                                onGenerateFlashcards = { q, text ->
                                    ResearchFlashcardEngine.generateFromReport(q, text)
                                    showFlashcards = true
                                }
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

            if (showNotebook) {
                NotebookSheet(
                    sessions = savedSessions,
                    onSelectSession = { session ->
                        sendQuery(session.query)
                    },
                    onDismiss = { showNotebook = false }
                )
            }

            if (showFlashcards) {
                FlashcardView(
                    onDismiss = { showFlashcards = false }
                )
            }
        }
    }
}

@Composable
private fun ModernSuggestionPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = DeepSeekTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HeroPromptCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = DeepSeekTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = DeepSeekTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun IconButtonSmall(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = DeepSeekGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun IconButtonIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(DeepSeekSurface)
            .border(1.dp, DeepSeekBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = DeepSeekTextSecondary,
            modifier = Modifier.size(16.dp)
        )
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
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(DeepSeekUserBubble)
                .border(1.dp, DeepSeekBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = text,
                color = DeepSeekTextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AssistantMessageCard(
    msg: ChatMessage,
    onCitationClick: (Citation) -> Unit,
    onGenerateFlashcards: (String, String) -> Unit = { _, _ -> }
) {
    val update = msg.update
    var isThoughtExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Assistant Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DeepSeekBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "CommonMSM",
                    color = DeepSeekTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DeepSeekBlue.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RAG",
                        color = DeepSeekBlueLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (update?.isComplete == true) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DeepSeekGreen)
                        .size(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // DeepSeek Style Thought Process Collapsible Box
        val hasSources = update != null && (update.instantPlaces.isNotEmpty() || update.instantSpecs.isNotEmpty() || update.retrievedSources.isNotEmpty())
        if (hasSources) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepSeekThinkingBg)
                    .border(1.dp, DeepSeekThinkingBorder, RoundedCornerShape(8.dp))
                    .clickable { isThoughtExpanded = !isThoughtExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Thought Process",
                                color = DeepSeekBlueLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val totalItems = (update?.instantPlaces?.size ?: 0) + (update?.instantSpecs?.size ?: 0) + (update?.retrievedSources?.size ?: 0)
                            Text(
                                text = "($totalItems sources retrieved)",
                                color = DeepSeekTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = if (isThoughtExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = DeepSeekTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = isThoughtExpanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            // Places in Thought Process
                            if (update.instantPlaces.isNotEmpty()) {
                                Text(
                                    text = "Retrieved Places (${update.instantPlaces.size}):",
                                    color = DeepSeekTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                SpatialRadarView(places = update.instantPlaces)
                                Spacer(modifier = Modifier.height(6.dp))
                                update.instantPlaces.take(3).forEach { place ->
                                    PlaceCard(place = place)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Specs in Thought Process
                            if (update.instantSpecs.isNotEmpty()) {
                                Text(
                                    text = "Retrieved Specifications (${update.instantSpecs.size}):",
                                    color = DeepSeekTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                update.instantSpecs.take(3).forEach { spec ->
                                    EipCard(spec = spec)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Response Body Text
        if (msg.text.isNotBlank()) {
            MarkdownRenderer(text = msg.text)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = DeepSeekBlue,
                    trackColor = DeepSeekBorder,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Thinking and synthesizing from local knowledge...",
                    color = DeepSeekTextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // Citations Row
        if (update != null && update.report != null && update.report.citations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Citations",
                color = DeepSeekTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
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

        // Action Toolbar (Flashcards, Copy, HUD)
        if (msg.text.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            val clipboardManager = LocalClipboardManager.current
            var isCopied by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: stats summary
                if (update != null && update.report != null) {
                    val stats = update.report.stats
                    Text(
                        text = "${String.format("%.1f", stats.tokensPerSecond)} t/s • ${stats.memoryUsedMb}MB RAM",
                        color = DeepSeekTextMuted,
                        fontSize = 11.sp
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Right side: Copy & Flashcards buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepSeekSurface)
                            .border(1.dp, DeepSeekBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                val query = update?.report?.query ?: "Research Topic"
                                onGenerateFlashcards(query, msg.text)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "+ Flashcards",
                            color = DeepSeekBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepSeekSurface)
                            .border(1.dp, DeepSeekBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                val fullReport = if (update?.report != null) {
                                    AirGapExporter.exportToMarkdown(update.report)
                                } else {
                                    msg.text
                                }
                                clipboardManager.setText(AnnotatedString(fullReport))
                                isCopied = true
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isCopied) DeepSeekGreen else DeepSeekTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCopied) "Copied" else "Copy",
                                color = if (isCopied) DeepSeekGreen else DeepSeekTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Performance Telemetry HUD
            if (update != null && update.report != null) {
                Spacer(modifier = Modifier.height(8.dp))
                PerformanceHUD(stats = update.report.stats)
            }
        }
    }
}

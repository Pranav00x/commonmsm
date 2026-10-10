package com.commonmsm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.data.models.ResearchSession
import com.commonmsm.engine.AuditLogger
import com.commonmsm.ui.components.ModernBadge
import com.commonmsm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotebookSheet(
    sessions: List<ResearchSession>,
    onSelectSession: (ResearchSession) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .border(1.dp, DeepSeekBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = DeepSeekBg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
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
                        text = "Research History",
                        color = DeepSeekTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DeepSeekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DeepSeekBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Audit Verification Bar
            var auditCopied by remember { mutableStateOf(false) }
            val clipboard = LocalClipboardManager.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audit Trail: ${if (AuditLogger.verifyChainIntegrity()) "Verified" else "Modified"}",
                    color = DeepSeekGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeepSeekSurface)
                        .border(1.dp, DeepSeekBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            clipboard.setText(AnnotatedString(AuditLogger.exportAuditCertificate()))
                            auditCopied = true
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (auditCopied) "Copied" else "Export Certificate",
                        color = DeepSeekTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DeepSeekBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved research sessions yet.",
                        color = DeepSeekTextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { s ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DeepSeekSurface)
                                .border(1.dp, DeepSeekBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectSession(s)
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ModernBadge(text = s.intent, color = DeepSeekBlueLight)
                                    Text(
                                        text = dateFormat.format(Date(s.timestamp)),
                                        color = DeepSeekTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = s.query,
                                    color = DeepSeekTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = s.summary,
                                    color = DeepSeekTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

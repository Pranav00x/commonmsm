package com.commonmsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commonmsm.ui.theme.*

@Composable
fun MarkdownRenderer(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val lines = text.split("\n")
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    inCodeBlock = false
                    val codeContent = codeBlockLines.joinToString("\n")
                    codeBlockLines.clear()
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepSeekSurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = codeContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = DeepSeekTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                continue
            }

            when {
                trimmed.startsWith("### ") -> {
                    val heading = trimmed.removePrefix("### ")
                    Text(
                        text = buildStyledMarkdown(heading),
                        color = DeepSeekBlueLight,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    val heading = trimmed.removePrefix("## ")
                    Text(
                        text = buildStyledMarkdown(heading),
                        color = DeepSeekTextPrimary,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    val heading = trimmed.removePrefix("# ")
                    Text(
                        text = buildStyledMarkdown(heading),
                        color = DeepSeekTextPrimary,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val content = trimmed.substring(2)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = DeepSeekBlue, fontWeight = FontWeight.Bold)) {
                                append("•  ")
                            }
                            append(buildStyledMarkdown(content))
                        },
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = DeepSeekTextPrimary,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
                trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                    val dotIdx = trimmed.indexOf('.')
                    val num = trimmed.substring(0, dotIdx + 1)
                    val content = trimmed.substring(dotIdx + 1).trim()
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = DeepSeekBlueLight, fontWeight = FontWeight.SemiBold)) {
                                append("$num ")
                            }
                            append(buildStyledMarkdown(content))
                        },
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = DeepSeekTextPrimary,
                        modifier = Modifier.padding(start = 6.dp, top = 3.dp, bottom = 3.dp)
                    )
                }
                trimmed.isNotBlank() -> {
                    Text(
                        text = buildStyledMarkdown(trimmed),
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = DeepSeekTextPrimary,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}

fun buildStyledMarkdown(text: String) = buildAnnotatedString {
    var cursor = 0
    val regex = Regex("""(\*\*(.*?)\*\*)|(\[(.*?)\])|(`(.*?)`)""")
    val matches = regex.findAll(text)

    for (match in matches) {
        val range = match.range
        if (range.first > cursor) {
            append(text.substring(cursor, range.first))
        }

        val raw = match.value
        when {
            raw.startsWith("**") && raw.endsWith("**") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = DeepSeekTextPrimary)) {
                    append(raw.removeSurrounding("**"))
                }
            }
            raw.startsWith("[") && raw.endsWith("]") -> {
                // Citation bracket [1], [2] or reference
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = DeepSeekBlueLight)) {
                    append(raw)
                }
            }
            raw.startsWith("`") && raw.endsWith("`") -> {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = DeepSeekAmber, fontSize = 12.sp)) {
                    append(raw.removeSurrounding("`"))
                }
            }
            else -> append(raw)
        }
        cursor = range.last + 1
    }

    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}

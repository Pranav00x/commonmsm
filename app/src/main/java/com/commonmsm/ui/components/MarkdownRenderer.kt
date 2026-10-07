package com.commonmsm.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        color = CommonMsmAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        color = CommonMsmOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                    )
                }
                trimmed.startsWith("• ") || trimmed.startsWith("- ") -> {
                    val content = trimmed.substring(2)
                    Text(
                        text = buildStyledMarkdown(content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
                trimmed.isNotBlank() -> {
                    Text(
                        text = buildStyledMarkdown(trimmed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
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
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                    append(raw.removeSurrounding("**"))
                }
            }
            raw.startsWith("[") && raw.endsWith("]") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = CommonMsmOrange)) {
                    append(raw)
                }
            }
            raw.startsWith("`") && raw.endsWith("`") -> {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = FlameYellow)) {
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

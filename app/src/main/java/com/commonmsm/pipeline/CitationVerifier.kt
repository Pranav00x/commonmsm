package com.commonmsm.pipeline

import com.commonmsm.data.models.Citation
import com.commonmsm.data.models.SearchResult

class CitationVerifier {

    fun extractAndVerifyCitations(
        text: String,
        sources: List<SearchResult>
    ): List<Citation> {
        val citationPattern = Regex("""\[(\d+)\]""")
        val matches = citationPattern.findAll(text)
        val citedIndices = matches.mapNotNull { it.groupValues[1].toIntOrNull() }.toSet()

        val verifiedCitations = mutableListOf<Citation>()

        for (idx in citedIndices) {
            val sourceIdx = idx - 1
            if (sourceIdx >= 0 && sourceIdx < sources.size) {
                val src = sources[sourceIdx]
                verifiedCitations.add(
                    Citation(
                        index = idx,
                        title = src.title,
                        source = src.sourceName,
                        snippet = src.snippet,
                        identifier = src.id
                    )
                )
            }
        }

        if (verifiedCitations.isEmpty() && sources.isNotEmpty()) {
            sources.take(3).forEachIndexed { i, src ->
                verifiedCitations.add(
                    Citation(
                        index = i + 1,
                        title = src.title,
                        source = src.sourceName,
                        snippet = src.snippet,
                        identifier = src.id
                    )
                )
            }
        }

        return verifiedCitations.sortedBy { it.index }
    }
}

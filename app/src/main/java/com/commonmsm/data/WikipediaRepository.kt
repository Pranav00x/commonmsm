package com.commonmsm.data

import com.commonmsm.data.models.SearchResult
import kotlin.math.ln

class WikipediaRepository {

    companion object {
        fun sanitizeFtsQuery(query: String): String {
            return FtsSanitizer.sanitizeFtsQuery(query)
        }
    }

    fun searchBm25(query: String, limit: Int = 5): List<SearchResult> {
        val db = DatabaseManager.getWikiDatabase() ?: return emptyList()
        val safeLimit = limit.coerceIn(1, 50)

        val sanitizedQuery = sanitizeFtsQuery(query)
        if (sanitizedQuery.isBlank()) return emptyList()

        val sql = """
            SELECT a.id, a.title, a.lead_text, a.body_text, a.pageviews, bm25(wiki_fts) as rank
            FROM wiki_fts f
            JOIN wiki_articles a ON f.rowid = a.rowid
            WHERE wiki_fts MATCH ?
            ORDER BY rank
            LIMIT ?
        """.trimIndent()

        val results = mutableListOf<SearchResult>()
        try {
            val cursor = db.rawQuery(sql, arrayOf(sanitizedQuery, safeLimit.toString()))
            cursor.use {
                val idCol = it.getColumnIndex("id")
                val titleCol = it.getColumnIndex("title")
                val leadCol = it.getColumnIndex("lead_text")
                val bodyCol = it.getColumnIndex("body_text")
                val pvCol = it.getColumnIndex("pageviews")
                val rankCol = it.getColumnIndex("rank")

                while (it.moveToNext()) {
                    val id = if (idCol >= 0) it.getString(idCol) else ""
                    val title = if (titleCol >= 0) it.getString(titleCol) else ""
                    val lead = if (leadCol >= 0) it.getString(leadCol) else ""
                    val body = if (bodyCol >= 0) it.getString(bodyCol) else ""
                    val pv = if (pvCol >= 0) it.getLong(pvCol) else 0L
                    val bm25Score = if (rankCol >= 0) it.getDouble(rankCol) else 0.0

                    val weightedScore = -bm25Score + (0.5 * ln((pv + 1).toDouble()))
                    val snippet = if (lead.isNotBlank()) lead else body.take(280) + "..."

                    results.add(
                        SearchResult(
                            id = id,
                            title = title,
                            sourceName = "Wikipedia",
                            snippet = snippet,
                            fullText = body,
                            score = weightedScore,
                            pageviews = pv
                        )
                    )
                }
            }
        } catch (e: Exception) {
            val tokens = query.split(Regex("[^a-zA-Z0-9]+")).filter { it.length >= 3 }
            val searchTerm = tokens.firstOrNull() ?: query.take(32)
            val fallbackSql = "SELECT id, title, lead_text, body_text, pageviews FROM wiki_articles WHERE title LIKE ? ESCAPE '\\' OR body_text LIKE ? ESCAPE '\\' LIMIT ?"
            val wildcard = FtsSanitizer.sanitizeLikePattern(searchTerm)
            val cursor = db.rawQuery(fallbackSql, arrayOf(wildcard, wildcard, safeLimit.toString()))
            cursor.use {
                while (it.moveToNext()) {
                    results.add(
                        SearchResult(
                            id = it.getString(0),
                            title = it.getString(1),
                            sourceName = "Wikipedia",
                            snippet = it.getString(2),
                            fullText = it.getString(3),
                            score = 1.0,
                            pageviews = it.getLong(4)
                        )
                    )
                }
            }
        }

        return results.sortedByDescending { it.score }
    }

    fun getArticleByTitle(title: String): SearchResult? {
        val db = DatabaseManager.getWikiDatabase() ?: return null
        val cursor = db.rawQuery(
            "SELECT id, title, lead_text, body_text, pageviews FROM wiki_articles WHERE LOWER(title) = LOWER(?) LIMIT 1",
            arrayOf(title)
        )
        return cursor.use {
            if (it.moveToNext()) {
                SearchResult(
                    id = it.getString(0),
                    title = it.getString(1),
                    sourceName = "Wikipedia",
                    snippet = it.getString(2),
                    fullText = it.getString(3),
                    score = 100.0,
                    pageviews = it.getLong(4)
                )
            } else null
        }
    }
}

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
        val results = mutableListOf<SearchResult>()
        val seenIds = mutableSetOf<String>()

        val sanitizedQuery = sanitizeFtsQuery(query)
        if (sanitizedQuery.isNotBlank()) {
            val sql = """
                SELECT a.id, a.title, a.lead_text, a.body_text, a.pageviews, bm25(wiki_fts) as rank
                FROM wiki_fts f
                JOIN wiki_articles a ON f.rowid = a.rowid
                WHERE wiki_fts MATCH ?
                ORDER BY rank
                LIMIT ?
            """.trimIndent()

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
                        if (id.isNotBlank() && seenIds.add(id)) {
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
                }
            } catch (_: Exception) {
                // Fall through to resilient multi-token search
            }
        }

        // Secondary fallback: Token-level keyword search on wiki_articles
        if (results.size < safeLimit) {
            val tokens = query.split(Regex("[^a-zA-Z0-9]+"))
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 && !isCommonStopWord(it) }

            for (token in tokens) {
                if (results.size >= safeLimit) break
                val wildcard = FtsSanitizer.sanitizeLikePattern(token)
                try {
                    val fallbackSql = """
                        SELECT id, title, lead_text, body_text, pageviews
                        FROM wiki_articles
                        WHERE title LIKE ? ESCAPE '\' OR lead_text LIKE ? ESCAPE '\' OR body_text LIKE ? ESCAPE '\'
                        ORDER BY pageviews DESC
                        LIMIT ?
                    """.trimIndent()
                    val cursor = db.rawQuery(fallbackSql, arrayOf(wildcard, wildcard, wildcard, (safeLimit - results.size).toString()))
                    cursor.use {
                        while (it.moveToNext()) {
                            val id = it.getString(0)
                            if (seenIds.add(id)) {
                                val title = it.getString(1)
                                val lead = it.getString(2)
                                val body = it.getString(3)
                                val pv = it.getLong(4)
                                val snippet = if (lead.isNotBlank()) lead else body.take(280) + "..."
                                results.add(
                                    SearchResult(
                                        id = id,
                                        title = title,
                                        sourceName = "Wikipedia",
                                        snippet = snippet,
                                        fullText = body,
                                        score = 10.0 + ln((pv + 1).toDouble()),
                                        pageviews = pv
                                    )
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        // Tertiary fallback: Cross-search Crypto EIP specs
        if (results.size < safeLimit) {
            val cryptoDb = DatabaseManager.getCryptoDatabase()
            if (cryptoDb != null) {
                val tokens = query.split(Regex("[^a-zA-Z0-9]+"))
                    .map { it.trim().lowercase() }
                    .filter { it.length >= 3 && !isCommonStopWord(it) }

                for (token in tokens.take(3)) {
                    if (results.size >= safeLimit) break
                    val wildcard = FtsSanitizer.sanitizeLikePattern(token)
                    try {
                        val eipSql = """
                            SELECT eip_number, title, summary, full_spec
                            FROM eips
                            WHERE title LIKE ? ESCAPE '\' OR summary LIKE ? ESCAPE '\' OR full_spec LIKE ? ESCAPE '\'
                            LIMIT ?
                        """.trimIndent()
                        val cursor = cryptoDb.rawQuery(eipSql, arrayOf(wildcard, wildcard, wildcard, (safeLimit - results.size).toString()))
                        cursor.use {
                            while (it.moveToNext()) {
                                val eipNum = it.getInt(0)
                                val id = "eip_$eipNum"
                                if (seenIds.add(id)) {
                                    val title = "EIP-$eipNum: ${it.getString(1)}"
                                    val summary = it.getString(2)
                                    val fullSpec = it.getString(3)
                                    results.add(
                                        SearchResult(
                                            id = id,
                                            title = title,
                                            sourceName = "Ethereum Specifications",
                                            snippet = summary,
                                            fullText = fullSpec,
                                            score = 8.0,
                                            pageviews = 1000L
                                        )
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        // Quaternary fallback: Cross-search Places database
        if (results.size < safeLimit) {
            val placesDb = DatabaseManager.getPlacesDatabase()
            if (placesDb != null) {
                val tokens = query.split(Regex("[^a-zA-Z0-9]+"))
                    .map { it.trim().lowercase() }
                    .filter { it.length >= 3 && !isCommonStopWord(it) }

                for (token in tokens.take(2)) {
                    if (results.size >= safeLimit) break
                    val wildcard = FtsSanitizer.sanitizeLikePattern(token)
                    try {
                        val placeSql = """
                            SELECT id, name, city, country, cuisine, diet_tags, address, opening_hours, fame_score
                            FROM places
                            WHERE name LIKE ? ESCAPE '\' OR city LIKE ? ESCAPE '\' OR cuisine LIKE ? ESCAPE '\' OR diet_tags LIKE ? ESCAPE '\'
                            ORDER BY fame_score DESC
                            LIMIT ?
                        """.trimIndent()
                        val cursor = placesDb.rawQuery(placeSql, arrayOf(wildcard, wildcard, wildcard, wildcard, (safeLimit - results.size).toString()))
                        cursor.use {
                            while (it.moveToNext()) {
                                val placeId = it.getString(0)
                                if (seenIds.add(placeId)) {
                                    val name = it.getString(1)
                                    val city = it.getString(2)
                                    val country = it.getString(3)
                                    val cuisine = it.getString(4)
                                    val diet = it.getString(5)
                                    val address = it.getString(6)
                                    val hours = it.getString(7)
                                    val fame = it.getDouble(8)
                                    val snippet = "$name ($city, $country) - $cuisine. Diet: $diet. Hours: $hours"
                                    val full = "$snippet. Address: $address."
                                    results.add(
                                        SearchResult(
                                            id = placeId,
                                            title = "$name ($city)",
                                            sourceName = "Places Directory",
                                            snippet = snippet,
                                            fullText = full,
                                            score = fame,
                                            pageviews = 500L
                                        )
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
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

    private fun isCommonStopWord(word: String): Boolean {
        return word in setOf(
            "the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "for",
            "tell", "me", "what", "best", "of", "to", "are", "how", "why", "with", "about",
            "can", "you", "does", "who", "when", "where", "from", "into"
        )
    }
}

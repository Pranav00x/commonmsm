package com.commonmsm.data

import com.commonmsm.data.models.EipEntity
import com.commonmsm.data.models.SearchResult

class CryptoSpecsRepository {

    fun getEip(eipNumber: Int): EipEntity? {
        val db = DatabaseManager.getCryptoDatabase() ?: return null
        val cursor = db.rawQuery(
            "SELECT eip_number, title, author, status, type, category, upgrade, summary, full_spec FROM eips WHERE eip_number = ? LIMIT 1",
            arrayOf(eipNumber.toString())
        )
        return cursor.use {
            if (it.moveToNext()) {
                EipEntity(
                    eipNumber = it.getInt(0),
                    title = it.getString(1),
                    author = it.getString(2),
                    status = it.getString(3),
                    type = it.getString(4),
                    category = it.getString(5),
                    networkUpgrade = it.getString(6),
                    summary = it.getString(7),
                    fullSpec = it.getString(8)
                )
            } else null
        }
    }

    fun searchSpecs(query: String, limit: Int = 5): List<EipEntity> {
        val db = DatabaseManager.getCryptoDatabase() ?: return emptyList()
        val list = mutableListOf<EipEntity>()
        val seenNumbers = mutableSetOf<Int>()

        // 1. Extract any explicit EIP/ERC numbers in the query
        val numberRegex = Regex("""\b(?:eip|erc)?[ -]?(\d{3,5})\b""", RegexOption.IGNORE_CASE)
        numberRegex.findAll(query).forEach { match ->
            match.groupValues[1].toIntOrNull()?.let { num ->
                if (!seenNumbers.contains(num)) {
                    getEip(num)?.let {
                        list.add(it)
                        seenNumbers.add(num)
                    }
                }
            }
        }

        // 2. Full-text search on eips_fts
        val tokens = query.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length >= 3 }
            .filter { !listOf("the", "and", "for", "with", "what", "compare", "tell").contains(it.lowercase()) }

        if (tokens.isNotEmpty() && list.size < limit) {
            val ftsQuery = tokens.joinToString(" OR ") { "\"$it\"*" }
            try {
                val ftsSql = """
                    SELECT e.eip_number, e.title, e.author, e.status, e.type, e.category, e.upgrade, e.summary, e.full_spec
                    FROM eips_fts f
                    JOIN eips e ON f.rowid = e.eip_number
                    WHERE eips_fts MATCH ?
                    LIMIT ?
                """.trimIndent()
                val cursor = db.rawQuery(ftsSql, arrayOf(ftsQuery, (limit - list.size).toString()))
                cursor.use {
                    while (it.moveToNext()) {
                        val num = it.getInt(0)
                        if (!seenNumbers.contains(num)) {
                            list.add(
                                EipEntity(
                                    eipNumber = num,
                                    title = it.getString(1),
                                    author = it.getString(2),
                                    status = it.getString(3),
                                    type = it.getString(4),
                                    category = it.getString(5),
                                    networkUpgrade = it.getString(6),
                                    summary = it.getString(7),
                                    fullSpec = it.getString(8)
                                )
                            )
                            seenNumbers.add(num)
                        }
                    }
                }
            } catch (e: Exception) {
                // 3. Fallback to token LIKE
                for (token in tokens.take(2)) {
                    if (list.size >= limit) break
                    val wildcard = "%$token%"
                    val cursor = db.rawQuery(
                        "SELECT eip_number, title, author, status, type, category, upgrade, summary, full_spec FROM eips WHERE title LIKE ? OR summary LIKE ? LIMIT ?",
                        arrayOf(wildcard, wildcard, (limit - list.size).toString())
                    )
                    cursor.use {
                        while (it.moveToNext()) {
                            val num = it.getInt(0)
                            if (!seenNumbers.contains(num)) {
                                list.add(
                                    EipEntity(
                                        eipNumber = num,
                                        title = it.getString(1),
                                        author = it.getString(2),
                                        status = it.getString(3),
                                        type = it.getString(4),
                                        category = it.getString(5),
                                        networkUpgrade = it.getString(6),
                                        summary = it.getString(7),
                                        fullSpec = it.getString(8)
                                    )
                                )
                                seenNumbers.add(num)
                            }
                        }
                    }
                }
            }
        }
        return list
    }

    fun toSearchResults(eips: List<EipEntity>): List<SearchResult> {
        return eips.map { eip ->
            SearchResult(
                id = "eip_${eip.eipNumber}",
                title = "EIP-${eip.eipNumber}: ${eip.title}",
                sourceName = "Ethereum Specs",
                snippet = "[Status: ${eip.status} | Upgrade: ${eip.networkUpgrade ?: "N/A"}] ${eip.summary}",
                fullText = eip.fullSpec,
                score = 50.0
            )
        }
    }
}

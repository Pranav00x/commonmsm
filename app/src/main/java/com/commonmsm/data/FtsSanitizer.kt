package com.commonmsm.data

object FtsSanitizer {

    private val STOP_WORDS = setOf(
        "the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "for",
        "tell", "me", "what", "best", "of", "to", "are", "how", "why", "with", "about"
    )

    private val FTS_RESERVED_OPERATORS = setOf(
        "and", "or", "not", "near", "match"
    )

    private const val MAX_QUERY_LENGTH = 256
    private const val MAX_TOKENS = 16

    /**
     * Sanitizes raw user query for safe execution inside SQLite FTS5 MATCH expressions.
     * Prevents syntax errors, token injection, operator hijacking, and AST blowups.
     */
    fun sanitizeFtsQuery(query: String): String {
        if (query.isBlank()) return ""

        val truncated = query.take(MAX_QUERY_LENGTH)

        // Extract alphanumeric tokens
        val rawTokens = truncated.split(Regex("[^a-zA-Z0-9]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val primaryTokens = rawTokens
            .filter { it.length >= 3 && !STOP_WORDS.contains(it.lowercase()) && !FTS_RESERVED_OPERATORS.contains(it.lowercase()) }
            .take(MAX_TOKENS)

        if (primaryTokens.isNotEmpty()) {
            return primaryTokens.joinToString(" OR ") { "\"$it\"*" }
        }

        // Fallback for short keywords (e.g. "AI", "ZK", "L2")
        val fallbackTokens = rawTokens
            .filter { it.length >= 2 && !FTS_RESERVED_OPERATORS.contains(it.lowercase()) }
            .take(MAX_TOKENS)

        if (fallbackTokens.isNotEmpty()) {
            return fallbackTokens.joinToString(" OR ") { "\"$it\"*" }
        }

        return ""
    }

    /**
     * Escapes SQLite LIKE pattern characters (% and _) with backslash.
     */
    fun sanitizeLikePattern(term: String): String {
        val sanitized = term.take(128)
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return "%$sanitized%"
    }
}

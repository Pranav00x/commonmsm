package com.commonmsm.engine

data class DietaryPhraseCard(
    val languageCode: String,
    val languageName: String,
    val headlinePhrase: String,
    val detailedExplanation: String,
    val allergensToAvoid: List<String>
)

data class TechnicalGlossaryTerm(
    val term: String,
    val domain: String,
    val definition: String,
    val relatedConcepts: List<String>
)

object OfflineGlossaryEngine {

    private val phraseCards = mapOf(
        "pt" to DietaryPhraseCard(
            languageCode = "pt",
            languageName = "PORTUGUESE (PORTUGAL)",
            headlinePhrase = "Sou vegano / Sou vegana.",
            detailedExplanation = "Nao como carne, peixe, marisco, leite, queijo, manteiga, ovos ou mel. Este prato contem algum ingrediente de origem animal?",
            allergensToAvoid = listOf("Carne", "Peixe", "Lacticinios", "Ovos", "Mel", "Banha")
        ),
        "ja" to DietaryPhraseCard(
            languageCode = "ja",
            languageName = "JAPANESE",
            headlinePhrase = "私はヴィーガン（完全菜食主義者）です。",
            detailedExplanation = "肉、魚、魚介類、乳製品、卵、はちみつ、かつおだし・煮干しなどの動物性だしは食べられません。これらに該当する原材料は含まれていますか？",
            allergensToAvoid = listOf("肉 (Meat)", "魚 (Fish)", "かつおだし (Bonito Dashi)", "乳製品 (Dairy)", "卵 (Eggs)")
        ),
        "de" to DietaryPhraseCard(
            languageCode = "de",
            languageName = "GERMAN",
            headlinePhrase = "Ich lebe vegan.",
            detailedExplanation = "Ich esse kein Fleisch, keinen Fisch, keine Meeresfruechte, keine Milchprodukte, keine Eier und keinen Honig. Enthaelt dieses Gericht tierische Zutaten?",
            allergensToAvoid = listOf("Fleisch", "Fisch", "Milchprodukte", "Eier", "Honig", "Schmalz")
        ),
        "es" to DietaryPhraseCard(
            languageCode = "es",
            languageName = "SPANISH",
            headlinePhrase = "Soy vegano / Soy vegana.",
            detailedExplanation = "No como carne, pescado, mariscos, productos lacteos, huevos ni miel. Este plato contiene algun ingrediente de origen animal?",
            allergensToAvoid = listOf("Carne", "Pescado", "Lacteos", "Huevos", "Miel", "Grasa animal")
        ),
        "it" to DietaryPhraseCard(
            languageCode = "it",
            languageName = "ITALIAN",
            headlinePhrase = "Sono vegano / Sono vegana.",
            detailedExplanation = "Non mangio carne, pesce, frutti di mare, latticini, uova o miele. Questo piatto contiene ingredienti di origine animale?",
            allergensToAvoid = listOf("Carne", "Pesce", "Latticini", "Uova", "Miele", "Strutto")
        )
    )

    private val technicalTerms = listOf(
        TechnicalGlossaryTerm(
            term = "EIP-7702",
            domain = "ETHEREUM_CORE",
            definition = "A transaction type (0x04) that temporarily points an Externally Owned Account (EOA) code hash to a smart contract address for the span of a single execution, enabling smart account batching without permanent migration.",
            relatedConcepts = listOf("ERC-4337", "EIP-3074", "Type 0x04", "Pectra")
        ),
        TechnicalGlossaryTerm(
            term = "KZG Commitments",
            domain = "CRYPTOGRAPHY",
            definition = "Kate-Zaverucha-Goldberg polynomial commitment scheme used in EIP-4844 blobs. Enables verifying polynomial evaluations in O(1) constant time with constant 48-byte proof size.",
            relatedConcepts = listOf("EIP-4844", "Data Availability", "BLOBHASH")
        ),
        TechnicalGlossaryTerm(
            term = "Falcon (FN-DSA)",
            domain = "POST_QUANTUM",
            definition = "A lattice-based digital signature scheme based on Gentry-Peikert-Vaikuntanathan (GPV) trapdoors over NTRU lattices. Produces ~666-byte compact signatures at the cost of floating-point FFT verification complexity.",
            relatedConcepts = listOf("ML-DSA", "NIST PQC", "FIPS 204", "NTRU")
        ),
        TechnicalGlossaryTerm(
            term = "Proposer-Builder Separation",
            domain = "CONSENSUS",
            definition = "Decoupling the validation and block proposing role from transaction ordering and block building to insulate home validators from MEV centralization pressures.",
            relatedConcepts = listOf("ePBS", "MEV-Boost", "Relays", "Builder Auctions")
        )
    )

    /**
     * Resolves dietary phrase card for a given country or ISO language code.
     */
    fun getDietaryCard(countryOrLang: String): DietaryPhraseCard {
        val key = when (countryOrLang.lowercase()) {
            "portugal", "pt" -> "pt"
            "japan", "ja", "jp" -> "ja"
            "germany", "de" -> "de"
            "spain", "es" -> "es"
            "italy", "it" -> "it"
            else -> "pt"
        }
        return phraseCards[key] ?: phraseCards["pt"]!!
    }

    /**
     * Searches technical terminology definitions offline.
     */
    fun lookupTechnicalTerm(query: String): TechnicalGlossaryTerm? {
        val trimmed = query.trim().lowercase()
        return technicalTerms.find { it.term.lowercase().contains(trimmed) || trimmed.contains(it.term.lowercase()) }
    }
}

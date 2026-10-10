package com.commonmsm.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File

object DatabaseManager {
    private const val TAG = "CommonMSM_DB"

    private var placesDb: SQLiteDatabase? = null
    private var wikiDb: SQLiteDatabase? = null
    private var cryptoDb: SQLiteDatabase? = null

    fun initialize(context: Context) {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dbDir = File(baseDir, "databases")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        openDatabases(dbDir, context)
    }

    fun reload(context: Context) {
        try {
            placesDb?.close()
            wikiDb?.close()
            cryptoDb?.close()
        } catch (_: Exception) {}
        initialize(context)
    }

    private fun openDatabases(dbDir: File, context: Context) {
        val placesFile = File(dbDir, "places.db")
        val wikiFile = File(dbDir, "wiki.db")
        val cryptoFile = File(dbDir, "crypto.db")

        // 1. Places DB
        placesDb = try {
            if (placesFile.exists()) {
                SQLiteDatabase.openDatabase(placesFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            } else {
                createInMemoryPlacesDb(context)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to bundled places database: ${e.message}")
            try { createInMemoryPlacesDb(context) } catch (t: Throwable) { null }
        }

        // 2. Wiki DB
        wikiDb = try {
            if (wikiFile.exists()) {
                SQLiteDatabase.openDatabase(wikiFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            } else {
                createInMemoryWikiDb(context)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to bundled wiki database: ${e.message}")
            try { createInMemoryWikiDb(context) } catch (t: Throwable) { null }
        }

        // 3. Crypto Specs DB
        cryptoDb = try {
            if (cryptoFile.exists()) {
                SQLiteDatabase.openDatabase(cryptoFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            } else {
                createInMemoryCryptoDb(context)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to bundled crypto database: ${e.message}")
            try { createInMemoryCryptoDb(context) } catch (t: Throwable) { null }
        }
    }

    fun getPlacesDatabase(): SQLiteDatabase? = placesDb
    fun getWikiDatabase(): SQLiteDatabase? = wikiDb
    fun getCryptoDatabase(): SQLiteDatabase? = cryptoDb

    private fun createInMemoryPlacesDb(context: Context): SQLiteDatabase {
        val db = SQLiteDatabase.create(null)
        db.execSQL("""
            CREATE TABLE places (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                city TEXT NOT NULL,
                country TEXT NOT NULL,
                latitude REAL,
                longitude REAL,
                category TEXT,
                cuisine TEXT,
                diet_tags TEXT,
                is_vegan INTEGER DEFAULT 0,
                opening_hours TEXT,
                address TEXT,
                wiki_title TEXT,
                fame_score REAL DEFAULT 0.0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_city_diet ON places(city, is_vegan)")

        val insert = """
            INSERT INTO places (id, name, city, country, latitude, longitude, category, cuisine, diet_tags, is_vegan, opening_hours, address, wiki_title, fame_score)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        db.compileStatement(insert).apply {
            // Lisbon Vegan Venues
            bindString(1, "lisbon_01"); bindString(2, "Ao 26 - Vegan Food Project"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7103); bindDouble(6, -9.1432); bindString(7, "restaurant"); bindString(8, "Portuguese / Fusion")
            bindString(9, "vegan,organic"); bindLong(10, 1); bindString(11, "12:30-23:00"); bindString(12, "Rua Vitor Cordon 26, Chiado"); bindString(13, "Ao 26"); bindDouble(14, 9.8)
            executeInsert()

            bindString(1, "lisbon_02"); bindString(2, "Kong - Food Made With Compassion"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7121); bindDouble(6, -9.1415); bindString(7, "restaurant"); bindString(8, "Comfort Food / Burgers")
            bindString(9, "vegan"); bindLong(10, 1); bindString(11, "12:00-22:30"); bindString(12, "Rua do Crucifixo 105"); bindString(13, "Kong Lisbon"); bindDouble(14, 9.5)
            executeInsert()

            bindString(1, "lisbon_03"); bindString(2, "Organi Chiado"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7099); bindDouble(6, -9.1420); bindString(7, "restaurant"); bindString(8, "Macrobiotic / Healthy")
            bindString(9, "vegan,gluten-free"); bindLong(10, 1); bindString(11, "12:00-22:00"); bindString(12, "Calcada Nova de Sao Francisco 2"); bindString(13, "Organi Chiado"); bindDouble(14, 9.3)
            executeInsert()

            bindString(1, "lisbon_04"); bindString(2, "The Green Spot (Parque das Nacoes)"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7675); bindDouble(6, -9.0967); bindString(7, "restaurant"); bindString(8, "Plant-Based Bowls"); bindString(9, "vegan,healthy"); bindLong(10, 1); bindString(11, "12:00-21:30"); bindString(12, "Alameda dos Oceanos 41"); bindString(13, "The Green Spot"); bindDouble(14, 8.9)
            executeInsert()

            // Berlin Vegan Venues
            bindString(1, "berlin_01"); bindString(2, "Lucky Leek"); bindString(3, "Berlin"); bindString(4, "Germany")
            bindDouble(5, 52.5372); bindDouble(6, 13.4184); bindString(7, "restaurant"); bindString(8, "Fine Dining / Plant-Based")
            bindString(9, "vegan,michelin"); bindLong(10, 1); bindString(11, "18:00-22:00"); bindString(12, "Kollwitzstrasse 54, Prenzlauer Berg"); bindString(13, "Lucky Leek"); bindDouble(14, 9.9)
            executeInsert()

            bindString(1, "berlin_02"); bindString(2, "1990 Vegan Living"); bindString(3, "Berlin"); bindString(4, "Germany")
            bindDouble(5, 52.5115); bindDouble(6, 13.4565); bindString(7, "restaurant"); bindString(8, "Vietnamese Tapas")
            bindString(9, "vegan"); bindLong(10, 1); bindString(11, "12:00-23:00"); bindString(12, "Krossener Str. 19, Friedrichshain"); bindString(13, "1990 Vegan Living"); bindDouble(14, 9.6)
            executeInsert()

            // Tokyo Vegan Venues
            bindString(1, "tokyo_01"); bindString(2, "Ain Soph. Soar"); bindString(3, "Tokyo"); bindString(4, "Japan")
            bindDouble(5, 35.7310); bindDouble(6, 139.7153); bindString(7, "restaurant"); bindString(8, "Vegan Fusion / Pancakes")
            bindString(9, "vegan,desserts"); bindLong(10, 1); bindString(11, "11:30-21:00"); bindString(12, "3-5-7 Higashiikebukuro, Toshima-ku"); bindString(13, "Ain Soph"); bindDouble(14, 9.7)
            executeInsert()

            bindString(1, "tokyo_02"); bindString(2, "T's Tantan (Tokyo Station)"); bindString(3, "Tokyo"); bindString(4, "Japan")
            bindDouble(5, 35.6812); bindDouble(6, 139.7671); bindString(7, "restaurant"); bindString(8, "Vegan Ramen / Gyoza")
            bindString(9, "vegan,ramen"); bindLong(10, 1); bindString(11, "10:00-22:00"); bindString(12, "Keiyo Street 1F, Tokyo Station"); bindString(13, "Ts Tantan"); bindDouble(14, 9.8)
            executeInsert()

            // Buenos Aires Vegan Venues
            bindString(1, "ba_01"); bindString(2, "Sacro"); bindString(3, "Buenos Aires"); bindString(4, "Argentina")
            bindDouble(5, -34.5828); bindDouble(6, -58.4347); bindString(7, "restaurant"); bindString(8, "Gourmet Plant-Based Dining")
            bindString(9, "vegan,high-end"); bindLong(10, 1); bindString(11, "12:00-01:00"); bindString(12, "Costa Rica 6038, Palermo Hollywood"); bindString(13, "Sacro BA"); bindDouble(14, 9.8)
            executeInsert()

            bindString(1, "ba_02"); bindString(2, "Buenos Aires Verde"); bindString(3, "Buenos Aires"); bindString(4, "Argentina")
            bindDouble(5, -34.5802); bindDouble(6, -58.4385); bindString(7, "restaurant"); bindString(8, "Organic & Raw Cuisine")
            bindString(9, "vegan,raw"); bindLong(10, 1); bindString(11, "09:00-00:00"); bindString(12, "Gorriti 5657, Palermo"); bindString(13, "BA Verde"); bindDouble(14, 9.4)
            executeInsert()

            // San Francisco Vegan Venues
            bindString(1, "sf_01"); bindString(2, "Shizen Vegan Sushi Bar"); bindString(3, "San Francisco"); bindString(4, "United States")
            bindDouble(5, 37.7683); bindDouble(6, -122.4216); bindString(7, "restaurant"); bindString(8, "Plant-Based Sushi & Izakaya")
            bindString(9, "vegan,japanese"); bindLong(10, 1); bindString(11, "17:00-22:00"); bindString(12, "370 14th St, Mission District"); bindString(13, "Shizen SF"); bindDouble(14, 9.9)
            executeInsert()

            // Chiang Mai Vegan Venues
            bindString(1, "cm_01"); bindString(2, "Anchan Vegetarian Restaurant"); bindString(3, "Chiang Mai"); bindString(4, "Thailand")
            bindDouble(5, 18.7961); bindDouble(6, 98.9682); bindString(7, "restaurant"); bindString(8, "Northern Thai / Plant-Based")
            bindString(9, "vegan,thai"); bindLong(10, 1); bindString(11, "11:30-20:30"); bindString(12, "Nimmanhaemin Soi 11"); bindString(13, "Anchan CM"); bindDouble(14, 9.6)
            executeInsert()
        }
        return db
    }

    private fun createInMemoryWikiDb(context: Context): SQLiteDatabase {
        val db = SQLiteDatabase.create(null)
        db.execSQL("""
            CREATE TABLE wiki_articles (
                id TEXT PRIMARY KEY,
                title TEXT UNIQUE,
                lead_text TEXT,
                body_text TEXT,
                pageviews INTEGER DEFAULT 0
            )
        """.trimIndent())
        val insert = "INSERT INTO wiki_articles (id, title, lead_text, body_text, pageviews) VALUES (?, ?, ?, ?, ?)"
        db.compileStatement(insert).apply {
            bindString(1, "art_pqc")
            bindString(2, "Post-Quantum Cryptography")
            bindString(3, "Post-quantum cryptography refers to cryptographic algorithms thought to be secure against attack by a quantum computer.")
            bindString(4, "In August 2024, NIST finalized its first set of post-quantum standards: FIPS 203 (ML-KEM / Kyber) for key encapsulation, FIPS 204 (ML-DSA / Dilithium) for digital signatures, and FIPS 205 (SLH-DSA / SPHINCS+). NIST is also standardizing Falcon (FN-DSA). For Ethereum, signature size is critical: Falcon signatures are ~666 bytes, significantly smaller than ML-DSA's ~2,420 bytes, making Falcon attractive for EVM calldata overhead despite complex floating-point verification.")
            bindLong(5, 120500)
            executeInsert()

            bindString(1, "art_lisbon")
            bindString(2, "Lisbon")
            bindString(3, "Lisbon is the capital and largest city of Portugal, with an estimated population of 548,703 within administrative limits.")
            bindString(4, "Lisbon has emerged as a major hub for plant-based and vegan dining in southern Europe. Historic districts like Chiado, Baixa, and Bairro Alto boast prominent vegan establishments such as Ao 26 Vegan Food Project, Kong, and Organi Chiado, which re-interpret traditional Portuguese pastéis and seafood classics using legumes and tofu.")
            bindLong(5, 450000)
            executeInsert()

            bindString(1, "art_pbs")
            bindString(2, "Proposer-Builder Separation")
            bindString(3, "Proposer-Builder Separation (PBS) is a design paradigm in Ethereum to mitigate Maximal Extractable Value (MEV) centralization.")
            bindString(4, "Under PBS, the role of block proposer (validator) is decoupled from the role of block builder. Builders run complex searcher algorithms to bundle transactions and construct blocks, then bid in an auction to have their block selected by proposers. Outside of MEV-Boost, enshringing PBS into the consensus protocol (ePBS) prevents validators from needing specialized MEV hardware and preserves decentralization.")
            bindLong(5, 185000)
            executeInsert()

            bindString(1, "art_rollups")
            bindString(2, "Zero-Knowledge vs Optimistic Rollups")
            bindString(3, "Rollups are Layer 2 scaling protocols that execute transactions off-chain while posting state roots and call data or blobs to Layer 1.")
            bindString(4, "Optimistic Rollups (e.g. Arbitrum, Optimism) assume transactions are valid by default and rely on a 7-day fraud-proof dispute window for withdrawals. Zero-Knowledge (ZK) Rollups (e.g. Starknet, zkSync, Scroll) generate cryptographic validity proofs (STARKs or SNARKs) that are verified immediately on-chain, eliminating the 7-day withdrawal delay at the cost of higher off-chain proof generation computational overhead.")
            bindLong(5, 290000)
            executeInsert()

            bindString(1, "art_crispr")
            bindString(2, "CRISPR-Cas9 and Prime Editing")
            bindString(3, "CRISPR-Cas9 and Prime Editing are targeted molecular gene editing technologies.")
            bindString(4, "CRISPR-Cas9 introduces double-strand breaks (DSBs) at loci directed by a single guide RNA (sgRNA), relying on cell repair mechanisms (NHEJ or HDR) that frequently induce uncontrolled indels. In contrast, Prime Editing couples a Cas9 nickase with an engineered reverse transcriptase guided by a pegRNA (prime editing guide RNA). Prime editing writes genetic modifications directly into the target DNA strand without creating double-strand breaks, dramatically reducing unintended insertion/deletion artifacts.")
            bindLong(5, 340000)
            executeInsert()

            bindString(1, "art_roman_concrete")
            bindString(2, "Roman Maritime Concrete and Pozzolanic Chemistry")
            bindString(3, "Ancient Roman maritime concrete exhibits extreme longevity compared to modern Portland cement in seawater environments.")
            bindString(4, "Roman builders mixed volcanic pozzolana ash with quicklime and seawater. When submerged in marine environments, percolating seawater dissolves components of the volcanic glass and lime clasts, triggering the autogenous crystallization of aluminum tobermorite and phillipsite. These interlocking mineral crystals grow over centuries to interlock pores and self-heal microcracks, whereas modern Portland cement degrades under marine sulfate and chloride attack.")
            bindLong(5, 220000)
            executeInsert()

            bindString(1, "art_bronze_age")
            bindString(2, "Late Bronze Age Collapse")
            bindString(3, "The Late Bronze Age Collapse was a period of widespread societal collapse across the Eastern Mediterranean around 1200 BCE.")
            bindString(4, "Around 1200 BCE, palatial civilizations including Mycenaean Greece, the Hittite Empire, and the New Kingdom of Egypt experienced catastrophic decline. Modern archaeological consensus attributes the collapse to a combination of factors: maritime invasions by the Sea Peoples disrupting trade corridors, multi-decade megadroughts documented in Mediterranean pollen cores, and the systemic failure of the international tin and copper supply chains required to produce bronze tools and weapons.")
            bindLong(5, 310000)
            executeInsert()
        }

        try {
            db.execSQL("CREATE VIRTUAL TABLE wiki_fts USING fts5(title, body)")
            db.execSQL("INSERT INTO wiki_fts(rowid, title, body) SELECT rowid, title, body_text FROM wiki_articles")
        } catch (e: Exception) {
            Log.w(TAG, "FTS5 initialization skipped for in-memory wiki: ${e.message}")
        }
        return db
    }

    private fun createInMemoryCryptoDb(context: Context): SQLiteDatabase {
        val db = SQLiteDatabase.create(null)
        db.execSQL("""
            CREATE TABLE eips (
                eip_number INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                author TEXT,
                status TEXT,
                type TEXT,
                category TEXT,
                upgrade TEXT,
                summary TEXT,
                full_spec TEXT
            )
        """.trimIndent())

        val insert = "INSERT INTO eips VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        db.compileStatement(insert).apply {
            bindLong(1, 7702)
            bindString(2, "Set EOA account code for one transaction")
            bindString(3, "Vitalik Buterin, Sam Wilson, Ansgar Dietrichs, Matt Garnett")
            bindString(4, "Final")
            bindString(5, "Standards Track")
            bindString(6, "Core")
            bindString(7, "Pectra (May 2025)")
            bindString(8, "Allows Externally Owned Accounts (EOAs) to temporarily set their contract code for the execution of a single transaction. This enables batching, gas sponsorship, and privilege delegation without permanent ERC-4337 migration.")
            bindString(9, "EIP-7702 introduces a new transaction type 0x04 containing an authorization list [[chain_id, address, nonce, y_parity, r, s], ...]. For each authorization, the authority's code is temporarily pointed to the designated implementation address, enabling smart contract wallet capabilities while maintaining existing EOA addresses and private keys. Key difference from ERC-4337: ERC-4337 operates entirely at the application layer via an alternative mempool (UserOperations) and EntryPoint contract, whereas EIP-7702 modifies core protocol transaction semantics.")
            executeInsert()

            bindLong(1, 4337)
            bindString(2, "Account Abstraction Using Alt Mempool")
            bindString(3, "Vitalik Buterin, Yoav Weiss, Kristof Gazso, Dror Tirosh, Shahaf Nacson, Tjaden Hess")
            bindString(4, "Final")
            bindString(5, "Standards Track")
            bindString(6, "ERC")
            bindString(7, "Application Layer")
            bindString(8, "Enables account abstraction without consensus-layer changes through an alternative mempool of UserOperations processed by Bundlers and validated by an EntryPoint contract.")
            bindString(9, "ERC-4337 achieves account abstraction without core Ethereum consensus modifications. Users send UserOperation objects to a separate P2P mempool. Specialized actors called Bundlers package these into a single handleOps call to a canonical EntryPoint contract. Wallets implement IAccount interface with validateUserOp function. Paymasters allow gas sponsorship and multi-token gas fees.")
            executeInsert()

            bindLong(1, 4844)
            bindString(2, "Shard Blob Transactions")
            bindString(3, "Vitalik Buterin, Dankrad Feist, Diederik Loerakker, George Kadianakis, Matt Garnett, Mofi Taiwo, Ansgar Dietrichs")
            bindString(4, "Final")
            bindString(5, "Standards Track")
            bindString(6, "Core")
            bindString(7, "Dencun (March 2024)")
            bindString(8, "Introduces temporary data blobs attached to transactions to dramatically reduce Layer 2 rollup calldata costs via polynomial KZG commitments.")
            bindString(9, "EIP-4844 introduces transaction type 0x03 with temporary data blobs (up to six 128KB blobs per block) that persist on consensus nodes for ~18 days. The EVM does not inspect the blob data directly; instead, contracts verify blob data validity using the BLOBHASH opcode and point evaluation precompiles at address 0x0A, slashing L2 rollup data posting costs by over 90%.")
            executeInsert()

            bindLong(1, 1559)
            bindString(2, "Fee market change for ETH 1.0 chain")
            bindString(3, "Vitalik Buterin, Eric Conner, Rick Dudley, Matthew Slipper, Ian Norden, Abdelhamid Bakhta")
            bindString(4, "Final")
            bindString(5, "Standards Track")
            bindString(6, "Core")
            bindString(7, "London (August 2021)")
            bindString(8, "Replaces first-price gas auctions with a dynamic BASEFEE that is burned, paired with an optional miner priority tip.")
            bindString(9, "EIP-1559 introduces transaction type 0x02 with max_fee_per_gas and max_priority_fee_per_gas. The protocol algorithmically adjusts BASEFEE dynamically targeting 50% block capacity (15M gas target, 30M gas cap). The BASEFEE is completely burned from the total ETH supply, reducing token velocity and introducing a deflationary mechanic during periods of high on-chain demand.")
            executeInsert()
        }

        try {
            db.execSQL("CREATE VIRTUAL TABLE eips_fts USING fts5(title, summary, full_spec)")
            db.execSQL("INSERT INTO eips_fts(rowid, title, summary, full_spec) SELECT eip_number, title, summary, full_spec FROM eips")
        } catch (e: Exception) {
            Log.w(TAG, "FTS5 initialization skipped for in-memory crypto: ${e.message}")
        }
        return db
    }

    fun saveResearchSession(session: com.commonmsm.data.models.ResearchSession) {
        val db = cryptoDb ?: placesDb ?: return
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS research_sessions (
                    id TEXT PRIMARY KEY,
                    query TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    intent TEXT NOT NULL,
                    sources_count INTEGER DEFAULT 0,
                    timestamp INTEGER NOT NULL
                )
            """.trimIndent())
            val stmt = db.compileStatement("INSERT OR REPLACE INTO research_sessions VALUES (?, ?, ?, ?, ?, ?)")
            stmt.bindString(1, session.id.take(64))
            stmt.bindString(2, session.query.take(512))
            stmt.bindString(3, session.summary.take(2048))
            stmt.bindString(4, session.intent.take(64))
            stmt.bindLong(5, session.sourcesCount.coerceIn(0, 1000).toLong())
            stmt.bindLong(6, session.timestamp)
            stmt.executeInsert()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save research session: ${e.message}")
        }
    }

    fun getRecentSessions(limit: Int = 10): List<com.commonmsm.data.models.ResearchSession> {
        val db = cryptoDb ?: placesDb ?: return emptyList()
        val safeLimit = limit.coerceIn(1, 100)
        val list = mutableListOf<com.commonmsm.data.models.ResearchSession>()
        try {
            val cursor = db.rawQuery("SELECT id, query, summary, intent, sources_count, timestamp FROM research_sessions ORDER BY timestamp DESC LIMIT ?", arrayOf(safeLimit.toString()))
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        com.commonmsm.data.models.ResearchSession(
                            id = it.getString(0),
                            query = it.getString(1),
                            summary = it.getString(2),
                            intent = it.getString(3),
                            sourcesCount = it.getInt(4),
                            timestamp = it.getLong(5)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Table may not exist yet
        }
        return list
    }
}

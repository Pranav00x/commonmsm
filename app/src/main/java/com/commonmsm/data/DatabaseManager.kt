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
            createInMemoryPlacesDb(context)
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
            createInMemoryWikiDb(context)
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
            createInMemoryCryptoDb(context)
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
            bindString(9, "vegan,organic"); bindLong(10, 1); bindString(11, "12:30-23:00"); bindString(12, "Rua Vitor Cordon 26, Chiado"); bindString(13, "Ao 26"); bindDouble(14, 9.6)
            executeInsert()

            bindString(1, "lisbon_02"); bindString(2, "Kong - Food Made With Compassion"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7121); bindDouble(6, -9.1415); bindString(7, "restaurant"); bindString(8, "Comfort Food / Burgers")
            bindString(9, "vegan"); bindLong(10, 1); bindString(11, "12:00-22:30"); bindString(12, "Rua do Crucifixo 105"); bindString(13, "Kong Lisbon"); bindDouble(14, 9.4)
            executeInsert()

            bindString(1, "lisbon_03"); bindString(2, "Organi Chiado"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7099); bindDouble(6, -9.1420); bindString(7, "restaurant"); bindString(8, "Macrobiotic / Healthy")
            bindString(9, "vegan,gluten-free"); bindLong(10, 1); bindString(11, "12:00-22:00"); bindString(12, "Calcada Nova de Sao Francisco 2"); bindString(13, "Organi Chiado"); bindDouble(14, 9.2)
            executeInsert()

            bindString(1, "lisbon_04"); bindString(2, "The Green Spot (Parque das Nacoes)"); bindString(3, "Lisbon"); bindString(4, "Portugal")
            bindDouble(5, 38.7675); bindDouble(6, -9.0967); bindString(7, "restaurant"); bindString(8, "Plant-Based Bowls"); bindString(9, "vegan,healthy"); bindLong(10, 1); bindString(11, "12:00-21:30"); bindString(12, "Alameda dos Oceanos 41"); bindString(13, "The Green Spot"); bindDouble(14, 8.8)
            executeInsert()

            // Berlin Vegan Venues
            bindString(1, "berlin_01"); bindString(2, "Lucky Leek"); bindString(3, "Berlin"); bindString(4, "Germany")
            bindDouble(5, 52.5372); bindDouble(6, 13.4184); bindString(7, "restaurant"); bindString(8, "Fine Dining / Plant-Based")
            bindString(9, "vegan,michelin"); bindLong(10, 1); bindString(11, "18:00-22:00"); bindString(12, "Kollwitzstrasse 54, Prenzlauer Berg"); bindString(13, "Lucky Leek"); bindDouble(14, 9.8)
            executeInsert()

            bindString(1, "berlin_02"); bindString(2, "1990 Vegan Living"); bindString(3, "Berlin"); bindString(4, "Germany")
            bindDouble(5, 52.5115); bindDouble(6, 13.4565); bindString(7, "restaurant"); bindString(8, "Vietnamese Tapas")
            bindString(9, "vegan"); bindLong(10, 1); bindString(11, "12:00-23:00"); bindString(12, "Krossener Str. 19, Friedrichshain"); bindString(13, "1990 Vegan Living"); bindDouble(14, 9.5)
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
        db.execSQL("""
            CREATE VIRTUAL TABLE wiki_fts USING fts5(
                title,
                body,
                content='wiki_articles',
                content_rowid='rowid'
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
        }
        db.execSQL("INSERT INTO wiki_fts(rowid, title, body) SELECT rowid, title, body_text FROM wiki_articles")
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
        db.execSQL("CREATE VIRTUAL TABLE eips_fts USING fts5(title, summary, full_spec)")

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
        }
        db.execSQL("INSERT INTO eips_fts(rowid, title, summary, full_spec) SELECT eip_number, title, summary, full_spec FROM eips")
        return db
    }
}

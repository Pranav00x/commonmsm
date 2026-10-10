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

        // 1. Places DB (create on disk if absent)
        placesDb = try {
            if (placesFile.exists()) {
                SQLiteDatabase.openDatabase(placesFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            } else {
                createPlacesDb(placesFile)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to in-memory places database: ${e.message}")
            try { createPlacesDb(null) } catch (t: Throwable) { null }
        }

        // 2. Wiki DB (create on disk if absent)
        wikiDb = try {
            if (wikiFile.exists()) {
                SQLiteDatabase.openDatabase(wikiFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            } else {
                createWikiDb(wikiFile)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to in-memory wiki database: ${e.message}")
            try { createWikiDb(null) } catch (t: Throwable) { null }
        }

        // 3. Crypto Specs DB (create on disk if absent)
        cryptoDb = try {
            if (cryptoFile.exists()) {
                SQLiteDatabase.openDatabase(cryptoFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            } else {
                createCryptoDb(cryptoFile)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to in-memory crypto database: ${e.message}")
            try { createCryptoDb(null) } catch (t: Throwable) { null }
        }
    }

    fun getPlacesDatabase(): SQLiteDatabase? = placesDb
    fun getWikiDatabase(): SQLiteDatabase? = wikiDb
    fun getCryptoDatabase(): SQLiteDatabase? = cryptoDb

    private fun createPlacesDb(file: File?): SQLiteDatabase {
        val db = if (file != null) {
            file.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(file, null)
        } else {
            SQLiteDatabase.create(null)
        }
        populatePlacesSchema(db)
        return db
    }

    private fun populatePlacesSchema(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS places (
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
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_city ON places(city)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_city_diet ON places(city, is_vegan)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_coords ON places(latitude, longitude)")

            val insert = """
                INSERT OR REPLACE INTO places (id, name, city, country, latitude, longitude, category, cuisine, diet_tags, is_vegan, opening_hours, address, wiki_title, fame_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            val stmt = db.compileStatement(insert)

            fun addPlace(
                id: String, name: String, city: String, country: String,
                lat: Double, lon: Double, category: String, cuisine: String,
                dietTags: String, isVegan: Long, hours: String, address: String,
                wikiTitle: String, fame: Double
            ) {
                stmt.bindString(1, id); stmt.bindString(2, name); stmt.bindString(3, city); stmt.bindString(4, country)
                stmt.bindDouble(5, lat); stmt.bindDouble(6, lon); stmt.bindString(7, category); stmt.bindString(8, cuisine)
                stmt.bindString(9, dietTags); stmt.bindLong(10, isVegan); stmt.bindString(11, hours); stmt.bindString(12, address)
                stmt.bindString(13, wikiTitle); stmt.bindDouble(14, fame)
                stmt.executeInsert()
            }

            // Lisbon
            addPlace("lis_01", "Ao 26 - Vegan Food Project", "Lisbon", "Portugal", 38.7103, -9.1432, "restaurant", "Portuguese / Fusion", "vegan,organic", 1, "12:30-23:00", "Rua Vitor Cordon 26, Chiado", "Ao 26", 9.8)
            addPlace("lis_02", "Kong - Food Made With Compassion", "Lisbon", "Portugal", 38.7121, -9.1415, "restaurant", "Comfort Food / Burgers", "vegan", 1, "12:00-22:30", "Rua do Crucifixo 105", "Kong Lisbon", 9.5)
            addPlace("lis_03", "Organi Chiado", "Lisbon", "Portugal", 38.7099, -9.1420, "restaurant", "Macrobiotic / Healthy", "vegan,gluten-free", 1, "12:00-22:00", "Calcada Nova de Sao Francisco 2", "Organi Chiado", 9.3)
            addPlace("lis_04", "The Green Spot (Parque das Nacoes)", "Lisbon", "Portugal", 38.7675, -9.0967, "restaurant", "Plant-Based Bowls", "vegan,healthy", 1, "12:00-21:30", "Alameda dos Oceanos 41", "The Green Spot", 8.9)
            addPlace("lis_05", "Ortea Botanical Collective", "Lisbon", "Portugal", 38.7077, -9.1554, "restaurant", "Artisan Vegan / Botanical", "vegan,gourmet", 1, "11:00-23:00", "Rua Dom Luis I 19", "Ortea", 9.4)
            addPlace("lis_06", "Vegan Junkies Chiado", "Lisbon", "Portugal", 38.7118, -9.1438, "restaurant", "Fast Casual / Tacos & Wings", "vegan,comfort", 1, "12:00-22:00", "Rua Luciano Cordeiro 28", "Vegan Junkies", 9.1)

            // Berlin
            addPlace("ber_01", "Lucky Leek", "Berlin", "Germany", 52.5372, 13.4184, "restaurant", "Plant-Based Fine Dining", "vegan,michelin", 1, "18:00-22:00", "Kollwitzstrasse 54, Prenzlauer Berg", "Lucky Leek", 9.9)
            addPlace("ber_02", "1990 Vegan Living", "Berlin", "Germany", 52.5115, 13.4565, "restaurant", "Vietnamese Tapas", "vegan", 1, "12:00-23:00", "Krossener Str. 19, Friedrichshain", "1990 Vegan Living", 9.6)
            addPlace("ber_03", "Brammibal's Donuts Maybachufer", "Berlin", "Germany", 52.4939, 13.4277, "cafe", "Bakery & Coffee", "vegan,pastry", 1, "09:00-20:00", "Maybachufer 8, Neukolln", "Brammibals", 9.4)
            addPlace("ber_04", "Secret Garden Berlin", "Berlin", "Germany", 52.5082, 13.4497, "restaurant", "Vegan Sushi Lounge", "vegan,japanese", 1, "12:00-23:00", "Warschauer Str. 33", "Secret Garden", 9.5)

            // Tokyo
            addPlace("tok_01", "Ain Soph. Soar", "Tokyo", "Japan", 35.7310, 139.7153, "restaurant", "Vegan Fusion / Pancakes", "vegan,desserts", 1, "11:30-21:00", "3-5-7 Higashiikebukuro, Toshima-ku", "Ain Soph", 9.7)
            addPlace("tok_02", "T's Tantan (Tokyo Station)", "Tokyo", "Japan", 35.6812, 139.7671, "restaurant", "Vegan Ramen & Gyoza", "vegan,ramen", 1, "10:00-22:00", "Keiyo Street 1F, Tokyo Station", "Ts Tantan", 9.8)
            addPlace("tok_03", "Saido Jiyugaoka", "Tokyo", "Japan", 35.6087, 139.6685, "restaurant", "Japanese Vegan Noodles", "vegan,traditional", 1, "11:30-20:00", "2-15-10 Jiyugaoka, Meguro-ku", "Saido", 9.6)

            // Buenos Aires
            addPlace("ba_01", "Sacro", "Buenos Aires", "Argentina", -34.5828, -58.4347, "restaurant", "Gourmet Plant-Based Dining", "vegan,high-end", 1, "12:00-01:00", "Costa Rica 6038, Palermo Hollywood", "Sacro BA", 9.8)
            addPlace("ba_02", "Buenos Aires Verde", "Buenos Aires", "Argentina", -34.5802, -58.4385, "restaurant", "Organic & Raw Cuisine", "vegan,raw", 1, "09:00-00:00", "Gorriti 5657, Palermo", "BA Verde", 9.4)
            addPlace("ba_03", "Mudra Plant Based", "Buenos Aires", "Argentina", -34.5772, -58.4312, "restaurant", "Contemporary Plant-Based", "vegan,fusion", 1, "10:00-23:30", "Av. Cordoba 3942", "Mudra", 9.5)

            // San Francisco
            addPlace("sf_01", "Shizen Vegan Sushi Bar", "San Francisco", "United States", 37.7683, -122.4216, "restaurant", "Plant-Based Sushi & Izakaya", "vegan,japanese", 1, "17:00-22:00", "370 14th St, Mission District", "Shizen SF", 9.9)
            addPlace("sf_02", "Wildseed Marina", "San Francisco", "United States", 37.7985, -122.4365, "restaurant", "Seasonal California Plant-Based", "vegan,organic", 1, "11:30-21:30", "2000 Union St", "Wildseed SF", 9.5)
            addPlace("sf_03", "Gracias Madre", "San Francisco", "United States", 37.7618, -122.4194, "restaurant", "Organic Mexican Vegan", "vegan,mexican", 1, "11:00-22:00", "2211 Mission St", "Gracias Madre", 9.3)

            // London
            addPlace("lon_01", "Mildreds Soho", "London", "United Kingdom", 51.5134, -0.1345, "restaurant", "Global Plant-Based Dining", "vegan,vegetarian", 1, "09:00-23:00", "45 Lexington St, Soho", "Mildreds", 9.6)
            addPlace("lon_02", "Purezza Camden", "London", "United Kingdom", 51.5392, -0.1425, "restaurant", "Sourdough Vegan Pizza", "vegan,italian", 1, "12:00-22:00", "43 Parkway, Camden Town", "Purezza", 9.7)
            addPlace("lon_03", "Gauthier Soho", "London", "United Kingdom", 51.5139, -0.1317, "restaurant", "French Fine Dining Vegan", "vegan,michelin", 1, "17:30-22:30", "21 Romilly St", "Gauthier", 9.8)

            // Paris
            addPlace("par_01", "Wild & The Moon Marais", "Paris", "France", 48.8624, 2.3618, "cafe", "Cold-Pressed Juices & Bowls", "vegan,gluten-free", 1, "08:00-19:00", "55 Rue Charlot, Le Marais", "Wild and The Moon", 9.4)
            addPlace("par_02", "Le Potager de Charlotte", "Paris", "France", 48.8789, 2.3475, "restaurant", "Gourmet French Plant Cuisine", "vegan,organic", 1, "12:00-22:30", "12 Rue de la Tour d'Auvergne", "Potager Charlotte", 9.7)

            // Chiang Mai
            addPlace("cm_01", "Anchan Vegetarian Restaurant", "Chiang Mai", "Thailand", 18.7961, 98.9682, "restaurant", "Northern Thai Plant-Based", "vegan,thai", 1, "11:30-20:30", "Nimmanhaemin Soi 11", "Anchan CM", 9.6)
            addPlace("cm_02", "Free Bird Cafe", "Chiang Mai", "Thailand", 18.7925, 98.9835, "cafe", "Social Enterprise Vegan Cafe", "vegan,organic", 1, "09:00-17:00", "Sirimangkalajarn Soi 9", "Free Bird", 9.3)

            // New York City
            addPlace("nyc_01", "Dirt Candy", "New York", "United States", 40.7176, -73.9912, "restaurant", "Vegetable-Forward Fine Dining", "vegan,michelin", 1, "17:30-22:30", "86 Allen St, Lower East Side", "Dirt Candy", 9.8)
            addPlace("nyc_02", "Avant Garden", "New York", "United States", 40.7265, -73.9842, "restaurant", "Plant-Based Wine & Dining", "vegan", 1, "17:00-23:00", "130 E 7th St, East Village", "Avant Garden", 9.6)

            // Singapore & Zurich
            addPlace("sg_01", "Whole Earth", "Singapore", "Singapore", 1.2789, 103.8445, "restaurant", "Peranakan Plant-Based", "vegan,michelin-bib", 1, "11:30-21:30", "76 Peck Seah St", "Whole Earth", 9.7)
            addPlace("zh_01", "Haus Hiltl", "Zurich", "Switzerland", 47.3734, 8.5365, "restaurant", "World's First Vegetarian Restaurant", "vegan,vegetarian", 1, "08:00-23:00", "Sihlstrasse 28", "Hiltl Zurich", 9.8)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun createWikiDb(file: File?): SQLiteDatabase {
        val db = if (file != null) {
            file.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(file, null)
        } else {
            SQLiteDatabase.create(null)
        }
        populateWikiSchema(db)
        return db
    }

    private fun populateWikiSchema(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS wiki_articles (
                    id TEXT PRIMARY KEY,
                    title TEXT UNIQUE,
                    lead_text TEXT,
                    body_text TEXT,
                    pageviews INTEGER DEFAULT 0
                )
            """.trimIndent())

            val insert = "INSERT OR REPLACE INTO wiki_articles (id, title, lead_text, body_text, pageviews) VALUES (?, ?, ?, ?, ?)"
            val stmt = db.compileStatement(insert)

            fun addArticle(id: String, title: String, lead: String, body: String, pv: Long) {
                stmt.bindString(1, id); stmt.bindString(2, title)
                stmt.bindString(3, lead); stmt.bindString(4, body)
                stmt.bindLong(5, pv)
                stmt.executeInsert()
            }

            // Cryptography & Blockchain
            addArticle(
                "art_pqc",
                "Post-Quantum Cryptography",
                "Post-quantum cryptography refers to cryptographic algorithms thought to be secure against attack by a quantum computer.",
                "In August 2024, NIST finalized its first set of post-quantum standards: FIPS 203 (ML-KEM / Kyber) for key encapsulation, FIPS 204 (ML-DSA / Dilithium) for digital signatures, and FIPS 205 (SLH-DSA / SPHINCS+). NIST is also standardizing Falcon (FN-DSA). For Ethereum, signature size is critical: Falcon signatures are ~666 bytes, significantly smaller than ML-DSA's ~2,420 bytes, making Falcon attractive for EVM calldata overhead despite complex floating-point verification.",
                120500
            )

            addArticle(
                "art_pbs",
                "Proposer-Builder Separation",
                "Proposer-Builder Separation (PBS) is a design paradigm in Ethereum to mitigate Maximal Extractable Value (MEV) centralization.",
                "Under PBS, the role of block proposer (validator) is decoupled from the role of block builder. Builders run complex searcher algorithms to bundle transactions and construct blocks, then bid in an auction to have their block selected by proposers. Outside of MEV-Boost, enshrining PBS into the consensus protocol (ePBS) prevents validators from needing specialized MEV hardware and preserves decentralization.",
                185000
            )

            addArticle(
                "art_rollups",
                "Zero-Knowledge vs Optimistic Rollups",
                "Rollups are Layer 2 scaling protocols that execute transactions off-chain while posting state roots and call data or blobs to Layer 1.",
                "Optimistic Rollups (e.g. Arbitrum, Optimism) assume transactions are valid by default and rely on a 7-day fraud-proof dispute window for withdrawals. Zero-Knowledge (ZK) Rollups (e.g. Starknet, zkSync, Scroll) generate cryptographic validity proofs (STARKs or SNARKs) that are verified immediately on-chain, eliminating the 7-day withdrawal delay at the cost of higher off-chain proof generation computational overhead.",
                290000
            )

            addArticle(
                "art_bitcoin",
                "Bitcoin and Proof of Work",
                "Bitcoin is a decentralized digital cryptocurrency introduced in 2008 by Satoshi Nakamoto, operating on a peer-to-peer network.",
                "Bitcoin relies on Nakamoto consensus driven by Proof of Work (PoW) using the SHA-256 hashing algorithm. Miners compete to find a block nonce producing a hash beneath the target difficulty, which adjusts every 2,016 blocks (~2 weeks). Transactions follow the Unspent Transaction Output (UTXO) model. The total supply is strictly capped at 21 million BTC, with the block issuance halving every 210,000 blocks (~4 years), creating programmed digital scarcity.",
                450000
            )

            addArticle(
                "art_ethereum",
                "Ethereum and Smart Contracts",
                "Ethereum is a decentralized open-source blockchain featuring smart contract functionality, powered by the Ethereum Virtual Machine (EVM).",
                "Proposed by Vitalik Buterin in 2013, Ethereum serves as a world computer. Transactions trigger deterministic state transitions executed across EVM bytecode. Gas metering prevents infinite loops (halting problem). Following The Merge in September 2022, Ethereum transitioned to Proof of Stake (PoS) consensus via the Beacon Chain, eliminating energy-intensive mining while securing over \$300B in assets with validator slashing conditions.",
                520000
            )

            addArticle(
                "art_zkp",
                "Zero-Knowledge Proofs",
                "A zero-knowledge proof allows a prover to convince a verifier that a mathematical statement is true without revealing any information beyond its validity.",
                "Zero-knowledge proofs satisfy three fundamental properties: completeness (honest provers convince honest verifiers), soundness (dishonest provers cannot convince honest verifiers), and zero-knowledge (no hidden information leaked). In modern cryptography, zk-SNARKs (Succinct Non-Interactive Arguments of Knowledge) and zk-STARKs (Scalable Transparent Arguments of Knowledge) represent the dominant constructions, widely applied in blockchain scalability, privacy, and identity protocols.",
                230000
            )

            // Science & Physics
            addArticle(
                "art_quantum",
                "Quantum Mechanics and Superposition",
                "Quantum mechanics is the fundamental theory in physics that provides a description of the physical properties of nature at the scale of atoms and subatomic particles.",
                "Key principles include wave-particle duality (de Broglie hypothesis), where entities exhibit both wave-like and particle-like properties. The Heisenberg Uncertainty Principle states that position and momentum cannot simultaneously be measured with arbitrary precision (Delta x * Delta p >= h-bar / 2). Quantum superposition allows states to exist as linear combinations of orthogonal eigenstates until wave function collapse occurs upon measurement.",
                380000
            )

            addArticle(
                "art_relativity",
                "General and Special Relativity",
                "Einstein's theories of relativity revolutionized the understanding of space, time, gravity, and the universe.",
                "Special Relativity (1905) posits that the laws of physics are invariant in all inertial frames and the speed of light in vacuum (c) is constant for all observers, yielding time dilation, length contraction, and mass-energy equivalence (E = mc^2). General Relativity (1915) describes gravity not as a Newtonian force, but as the geometric curvature of 4D spacetime induced by mass and energy, governed by Einstein's field equations G_mu_nu = (8 * pi * G / c^4) * T_mu_nu.",
                410000
            )

            addArticle(
                "art_black_holes",
                "Black Holes and Event Horizons",
                "A black hole is a region of spacetime where gravity is so strong that nothing, including light and other electromagnetic waves, has enough energy to escape its event horizon.",
                "The boundary is the event horizon, defined by the Schwarzschild radius r_s = 2GM / c^2 for non-rotating masses. At the center lies a gravitational singularity of theoretically infinite density. In 1974, Stephen Hawking demonstrated that quantum fluctuations near the event horizon emit blackbody thermal radiation (Hawking radiation), implying black holes eventually evaporate over cosmological timescales.",
                340000
            )

            addArticle(
                "art_thermo",
                "Thermodynamics and Entropy",
                "Thermodynamics is the branch of physics that deals with heat, work, temperature, and their relation to energy and radiation.",
                "The Four Laws govern thermal systems: Zeroth Law defines thermal equilibrium; First Law establishes conservation of energy (Delta U = Q - W); Second Law states that the total entropy of an isolated system never decreases over time (Delta S >= 0), defining the thermodynamic arrow of time; Third Law states that entropy approaches a constant value as temperature approaches absolute zero (0 Kelvin / -273.15 C).",
                270000
            )

            addArticle(
                "art_light",
                "Speed of Light and Photons",
                "The speed of light in vacuum, denoted c, is a universal physical constant exactly equal to 299,792,458 meters per second.",
                "According to special relativity, c is the maximum speed at which all conventional matter, energy, and information can travel in spacetime. Photons, the massless quanta of the electromagnetic field, travel at c in vacuum. When light travels through transparent media like water or glass, its phase velocity decreases to v = c / n (where n is refractive index), allowing charged particles exceeding this reduced phase velocity to emit Cherenkov radiation.",
                320000
            )

            addArticle(
                "art_gravity",
                "Gravity and Gravitational Waves",
                "Gravity is the natural phenomenon by which all things with mass or energy are attracted toward one another.",
                "In classical mechanics, Newton's law states that gravitational attraction is proportional to the product of masses and inversely proportional to the square of their distance (F = G * m1 * m2 / r^2). In General Relativity, accelerated masses produce ripples in the metric tensor of spacetime called gravitational waves, which travel at light speed. The first direct observation occurred in 2015 by the LIGO/Virgo collaboration from a binary black hole merger.",
                260000
            )

            addArticle(
                "art_fusion",
                "Nuclear Fusion and Fission",
                "Nuclear reactions release vast energy by transforming atomic nuclei according to mass defect and binding energy curves.",
                "Nuclear fission splits heavy nuclei (such as Uranium-235 or Plutonium-239) into lighter fragments upon neutron capture, producing chain reactions utilized in commercial nuclear reactors. Nuclear fusion combines light nuclei (such as Deuterium and Tritium) to form Helium-4 and a high-energy neutron (17.6 MeV total). Fusion powers the Sun and is actively pursued in magnetic confinement tokamaks (ITER) and inertial confinement facilities.",
                280000
            )

            addArticle(
                "art_big_bang",
                "Big Bang and Cosmology",
                "The Big Bang is the prevailing cosmological model explaining the origin and expansion of the observable universe.",
                "Originating approximately 13.8 billion years ago from an extremely hot, dense initial state, the universe underwent cosmic inflation followed by metric expansion. Major observational pillars confirming the model include the Cosmic Microwave Background (CMB) radiation at 2.725 Kelvin discovered by Penzias and Wilson, the cosmological redshift of distant galaxies (Hubble's law), and primordial Big Bang nucleosynthesis light-element abundances.",
                310000
            )

            // Biology & Life Sciences
            addArticle(
                "art_photosynthesis",
                "Photosynthesis and Cellular Energy",
                "Photosynthesis is the biological process used by plants, algae, and cyanobacteria to convert light energy into chemical energy.",
                "The process occurs within chloroplasts in two stages: Light-dependent reactions in the thylakoid membrane capture photons via chlorophyll in Photosystems II and I, splitting water (photolysis) to generate oxygen, ATP, and NADPH. The light-independent Calvin cycle in the stroma utilizes the enzyme RuBisCO to fix carbon dioxide into 3-carbon sugars (G3P), synthesizing glucose that fuels cellular metabolism.",
                330000
            )

            addArticle(
                "art_respiration",
                "Cellular Respiration and Mitochondria",
                "Cellular respiration is a set of metabolic reactions that convert biochemical energy from nutrients into adenosine triphosphate (ATP).",
                "Respiration proceeds across three major biochemical pathways: Glycolysis in the cytoplasm converts glucose to pyruvate, generating 2 ATP and 2 NADH. The Citric Acid (Krebs) cycle in the mitochondrial matrix oxidizes acetyl-CoA, producing NADH and FADH2. Oxidative phosphorylation along the inner mitochondrial membrane uses the electron transport chain and ATP synthase proton gradient to yield ~30-32 ATP per glucose molecule.",
                290000
            )

            addArticle(
                "art_dna",
                "DNA and Molecular Genetics",
                "Deoxyribonucleic acid (DNA) is a polymer composed of two polynucleotide chains that coil around each other to form a double helix.",
                "Discovered structurally by Watson, Crick, and Franklin in 1953, DNA stores genetic instructions across four nitrogenous bases: Adenine (A), Thymine (T), Guanine (G), and Cytosine (C), with complementary base pairing A-T and G-C. The Central Dogma of molecular biology describes the directional transfer of sequential genetic information: DNA replication -> RNA transcription via RNA polymerase -> Protein translation on ribosomes.",
                370000
            )

            addArticle(
                "art_crispr",
                "CRISPR-Cas9 and Prime Editing",
                "CRISPR-Cas9 and Prime Editing are targeted molecular gene editing technologies.",
                "CRISPR-Cas9 introduces double-strand breaks (DSBs) at loci directed by a single guide RNA (sgRNA), relying on cell repair mechanisms (NHEJ or HDR) that frequently induce uncontrolled indels. In contrast, Prime Editing couples a Cas9 nickase with an engineered reverse transcriptase guided by a pegRNA (prime editing guide RNA). Prime editing writes genetic modifications directly into the target DNA strand without creating double-strand breaks, dramatically reducing unintended insertion/deletion artifacts.",
                340000
            )

            addArticle(
                "art_evolution",
                "Evolution by Natural Selection",
                "Evolution is change in the heritable characteristics of biological populations over successive generations.",
                "Formulated by Charles Darwin and Alfred Russel Wallace, natural selection explains adaptation: organisms with traits better suited to their environmental conditions have higher reproductive fitness, passing advantageous alleles to offspring. Combined with Mendelian genetics into the Modern Synthesis, evolution operates through four core forces: natural selection, genetic drift, gene flow, and random genetic mutation.",
                350000
            )

            addArticle(
                "art_immunology",
                "Immunology and Vaccines",
                "Immunology is the study of immune systems across all organisms, protecting against pathogens.",
                "The immune system comprises two branches: Innate immunity provides non-specific rapid defense via physical barriers, phagocytes, and complement proteins. Adaptive immunity develops targeted antigen-specific responses via B-cells (producing neutralizing antibodies) and T-cells (cytotoxic CD8+ destroying infected cells, CD4+ helper coordinating responses). Vaccines expose the immune system to attenuated antigens or mRNA blueprints, establishing long-lived immunological memory.",
                310000
            )

            // Computer Science & AI
            addArticle(
                "art_ml",
                "Machine Learning and Neural Networks",
                "Machine learning is a field of artificial intelligence focused on algorithms that learn patterns from data to make predictions.",
                "Supervised learning trains models on labeled datasets by minimizing a loss function via backpropagation and stochastic gradient descent (SGD). Artificial Neural Networks consist of interconnected layers of nodes (perceptrons) computing weighted linear sums followed by non-linear activation functions (ReLU, GELU). Deep learning expands these architectures to tens or hundreds of layers, enabling hierarchical representation learning across vision, audio, and language.",
                490000
            )

            addArticle(
                "art_llm",
                "Large Language Models and Transformers",
                "Large language models (LLMs) are deep learning models trained on vast text corpora capable of natural language understanding and generation.",
                "Modern LLMs rely on the Transformer architecture introduced by Vaswani et al. in 2017 ('Attention Is All You Need'). Replacing recurrent networks with scaled dot-product self-attention (Attention(Q, K, V) = softmax(QK^T / sqrt(d_k)) * V) enables parallelized training across massive GPU clusters. Pre-trained on autoregressive next-token prediction, models are post-trained with instruction fine-tuning and Reinforcement Learning from Human Feedback (RLHF).",
                580000
            )

            addArticle(
                "art_moe",
                "Mixture of Experts Architecture",
                "Mixture of Experts (MoE) is a neural network architecture that activates only a subset of parameters for each input token.",
                "Instead of passing every token through a uniform dense Feed-Forward Network (FFN), an MoE layer replaces the FFN with N parallel 'expert' sub-networks and a lightweight router (gating network). The router selects the top-k experts (typically k=2 of 8 or 16) per token. This decouples total model capacity (e.g. 35B parameters) from active inference FLOPs (e.g. 3.5B active parameters), providing high factual recall while preserving mobile latency and thermal headroom.",
                260000
            )

            addArticle(
                "art_rag",
                "Retrieval-Augmented Generation",
                "Retrieval-Augmented Generation (RAG) is a technique that enhances language model generation by retrieving external knowledge passages.",
                "Rather than relying solely on parametric memory stored in model weights, RAG introduces an external knowledge retrieval phase. When a user submits a prompt, an index (such as BM25 inverted lexical indices or dense vector embeddings) retrieves top-k relevant ground-truth passages. The system prepends these passages into the prompt context, constraining the language model to synthesize responses grounded in verified source citations.",
                310000
            )

            addArticle(
                "art_quantization",
                "Quantization and GGUF Format",
                "Model quantization reduces the bit-precision of neural network weights to minimize memory bandwidth and footprint on consumer hardware.",
                "Full precision weights (FP32, 4 bytes/weight) or half precision (FP16/BF16, 2 bytes/weight) require substantial memory. Post-training quantization techniques compress weights to 4-bit (INT4, ~0.5 bytes/weight) or 8-bit integers. The GGUF container format developed by Georgi Gerganov and the llama.cpp community stores quantized tensors, vocabulary tokens, and hyperparameter metadata in a single binary file optimized for mmap zero-copy loading on mobile and edge devices.",
                240000
            )

            addArticle(
                "art_os",
                "Operating Systems and Virtual Memory",
                "An operating system (OS) is system software that manages computer hardware, software resources, and provides common services for programs.",
                "Core responsibilities include kernel-space hardware abstraction, process scheduling (preemptive multitasking), and virtual memory management. Virtual memory translates process virtual addresses to physical RAM pages via page tables and the hardware Memory Management Unit (MMU). Android utilizes the Linux kernel, enforcing process sandboxing (SELinux), per-user UID isolation, and aggressive memory management via the Low Memory Killer (LMK).",
                290000
            )

            addArticle(
                "art_compilers",
                "Compilers and Abstract Syntax Trees",
                "A compiler is a computer program that translates computer code written in one programming language into machine code or bytecode.",
                "The compilation pipeline comprises frontend, optimizer, and backend stages. Lexical analysis tokenizes source text; syntactic analysis builds an Abstract Syntax Tree (AST); semantic analysis enforces type checking. Modern compilers (like LLVM and GCC) convert ASTs into an Intermediate Representation (IR), apply optimization passes (dead code elimination, loop vectorization, constant folding), and generate target machine assembly.",
                220000
            )

            addArticle(
                "art_networking",
                "Computer Networking and TCP/IP",
                "The Internet protocol suite (TCP/IP) defines the networking protocols and communications architecture for the global internet.",
                "Organized into four abstraction layers: Link Layer (Ethernet, Wi-Fi framing); Internet Layer (IP routing, IPv4/IPv6 packet addressing); Transport Layer (TCP connection-oriented reliable byte stream with three-way handshake and congestion control, UDP connectionless datagrams); Application Layer (HTTP/3, DNS, TLS, SSH). The Domain Name System (DNS) translates human-readable domain names into IP addresses via hierarchical recursive resolvers.",
                330000
            )

            // History, Materials & Civilizations
            addArticle(
                "art_roman_concrete",
                "Roman Maritime Concrete and Pozzolanic Chemistry",
                "Ancient Roman maritime concrete exhibits extreme longevity compared to modern Portland cement in seawater environments.",
                "Roman builders mixed volcanic pozzolana ash with quicklime and seawater. When submerged in marine environments, percolating seawater dissolves components of the volcanic glass and lime clasts, triggering the autogenous crystallization of aluminum tobermorite and phillipsite. These interlocking mineral crystals grow over centuries to interlock pores and self-heal microcracks, whereas modern Portland cement degrades under marine sulfate and chloride attack.",
                220000
            )

            addArticle(
                "art_bronze_age",
                "Late Bronze Age Collapse",
                "The Late Bronze Age Collapse was a period of widespread societal collapse across the Eastern Mediterranean around 1200 BCE.",
                "Around 1200 BCE, palatial civilizations including Mycenaean Greece, the Hittite Empire, and the New Kingdom of Egypt experienced catastrophic decline. Modern archaeological consensus attributes the collapse to a combination of factors: maritime invasions by the Sea Peoples disrupting trade corridors, multi-decade megadroughts documented in Mediterranean pollen cores, and the systemic failure of the international tin and copper supply chains required to produce bronze tools and weapons.",
                310000
            )

            addArticle(
                "art_rome",
                "Ancient Rome and Classical Republic",
                "Ancient Rome grew from an Italic settlement on the Tiber River into an expansive civilization dominating the Mediterranean basin.",
                "The Roman Republic (509-27 BCE) established representative institutions including the Senate, magistrates (consuls, praetors), and popular assemblies with constitutional checks and balances. Following civil wars and the rise of Julius Caesar and Augustus, the Roman Empire ushered in the Pax Romana, engineering unprecedented infrastructure: paved road networks, aqueducts, arches, and codified civil law systems that form the foundation of modern jurisprudence.",
                360000
            )

            addArticle(
                "art_athens",
                "Ancient Greece and Athenian Democracy",
                "Classical Athens in the 5th century BCE pioneered the world's first recorded direct democracy and a flourishing intellectual tradition.",
                "Instituted by Cleisthenes in 508 BCE and expanded by Pericles, Athenian democracy empowered male citizens in the Ecclesia (assembly) to vote directly on legislation, military strategy, and ostracism. Athens fostered foundational Western philosophy through Socrates, Plato, and Aristotle, theatrical tragedy and comedy (Sophocles, Euripides), and historiography (Herodotus, Thucydides).",
                280000
            )

            addArticle(
                "art_industrial",
                "The Industrial Revolution",
                "The Industrial Revolution was the transition to new manufacturing processes in Great Britain, continental Europe, and the United States from around 1760 to 1840.",
                "Key technological innovations included mechanization of textile spinning (Hargreaves' Spinning Jenny, Arkwright's Water Frame), the development of Watt's commercial steam engine, and the transition from wood and charcoal to coal-powered iron smelting. This transition catalyzed global urbanization, factory production lines, modern capitalism, and rapid expansion of transportation networks through railways and steamships.",
                320000
            )

            // Economics & Practical Science
            addArticle(
                "art_inflation",
                "Economics: Inflation and Monetary Policy",
                "Inflation is the general increase in the prices of goods and services in an economy over a period of time.",
                "Measured by consumer price indices (CPI), inflation erodes the purchasing power of money. Demand-pull inflation occurs when aggregate demand exceeds productive capacity; cost-push inflation arises from supply chain shocks or rising input costs. Central banks manage inflation through monetary policy: adjusting short-term policy interest rates, managing reserve requirements, and employing quantitative easing or tightening to balance price stability with full employment.",
                310000
            )

            addArticle(
                "art_supply_demand",
                "Supply and Demand Economics",
                "Supply and demand is an economic model of price determination in a competitive market.",
                "The Law of Demand states that, ceteris paribus, higher prices lead to lower quantity demanded. The Law of Supply states that higher prices incentivize producers to supply greater quantities. The intersection of supply and demand curves establishes market equilibrium price and quantity, maximizing social welfare (consumer surplus plus producer surplus) in the absence of externalities or market distortions.",
                270000
            )

            addArticle(
                "art_stoicism",
                "Philosophy of Stoicism",
                "Stoicism is a Hellenistic school of philosophy founded in Athens by Zeno of Citium in the early 3rd century BCE.",
                "Stoicism teaches that virtue (wisdom, courage, justice, temperance) is the sole true good, and that happiness (eudaimonia) is achieved by living in accordance with nature. A core tenet is the dichotomy of control: distinguishing between things within our power (our beliefs, desires, actions) and things outside our power (external events, other people's actions). Prominent Roman Stoics include Marcus Aurelius (Meditations), Seneca the Younger (Letters), and Epictetus (Discourses).",
                290000
            )

            addArticle(
                "art_flight",
                "Aerodynamics and How Airplanes Fly",
                "Flight in fixed-wing aircraft relies on aerodynamic forces generated by air moving across airfoil-shaped wings.",
                "Four fundamental forces govern flight: Lift, Weight (gravity), Thrust, and Drag. Wings are engineered with an airfoil cross-section: as the wing moves forward, airflow is deflected downward, and air accelerates over the cambered upper surface, generating lower pressure according to Bernoulli's principle and Newton's Third Law of Motion (downward action of air yields an equal upward reaction on the wing). Thrust provided by jet engines or propellers overcomes drag to sustain forward velocity.",
                280000
            )

            addArticle(
                "art_sky_blue",
                "Optics and Why the Sky is Blue",
                "The blue color of the daytime sky is caused by Rayleigh scattering of sunlight by atmospheric gas molecules.",
                "Sunlight comprises all wavelengths of visible light. When sunlight enters Earth's atmosphere, it interacts with nitrogen and oxygen molecules much smaller than the wavelength of light. Rayleigh scattering intensity is inversely proportional to the fourth power of the wavelength (I ~ 1 / lambda^4). Consequently, shorter blue wavelengths (~400-475 nm) scatter approximately ten times more efficiently than longer red wavelengths (~650-700 nm), dispersing diffuse blue light across the sky.",
                260000
            )

            // Global Cities
            addArticle("art_lisbon", "Lisbon", "Lisbon is the capital and largest city of Portugal, situated at the mouth of the Tagus River.", "Lisbon has emerged as a major European center for technology, maritime heritage, and plant-based gastronomy. Historic districts like Chiado, Baixa, and Bairro Alto boast celebrated vegan dining venues such as Ao 26 Vegan Food Project, Kong, and Organi Chiado.", 450000)
            addArticle("art_berlin", "Berlin", "Berlin is the capital and largest city of Germany by both area and population.", "Renowned for its culture, politics, and technology scene, Berlin is widely considered Europe's vegan capital, featuring hundreds of dedicated plant-based venues including Lucky Leek in Prenzlauer Berg and 1990 Vegan Living in Friedrichshain.", 390000)
            addArticle("art_tokyo", "Tokyo", "Tokyo is the capital and most populous metropolis of Japan.", "Blending ultramodern skyscrapers with historic shrines, Tokyo is a culinary epicenter featuring pioneering vegan noodle and dining shops like T's Tantan inside Tokyo Station and Ain Soph in Ikebukuro.", 480000)
            addArticle("art_paris", "Paris", "Paris is the capital and most populous city of France.", "Situated along the Seine River, Paris is renowned for art, fashion, gastronomy, and culture. The city features celebrated organic plant-based bistros including Wild & The Moon and Le Potager de Charlotte.", 460000)
            addArticle("art_london", "London", "London is the capital and largest city of England and the United Kingdom.", "Standing on the River Thames, London is a pre-eminent global city in finance, commerce, and higher education, hosting renowned plant-based venues like Mildreds in Soho and Purezza in Camden.", 510000)
            addArticle("art_sf", "San Francisco", "San Francisco is the commercial, financial, and cultural center of Northern California.", "Located adjacent to Silicon Valley, San Francisco leads technological and biotechnology development. Its vibrant Mission District and Marina feature premier dining including Shizen Vegan Sushi Bar and Wildseed.", 370000)
            addArticle("art_nyc", "New York City", "New York City is the most populous city in the United States.", "Comprising five boroughs, NYC is a global cultural, financial, and entertainment capital, home to Michelin-starred plant-based dining like Dirt Candy and East Village favorites like Avant Garden.", 560000)
            addArticle("art_buenos_aires", "Buenos Aires", "Buenos Aires is the capital and largest city of Argentina.", "Known for its rich cultural life and European-style architecture, Buenos Aires features prominent plant-based culinary hubs in Palermo Hollywood, notably Sacro and Buenos Aires Verde.", 320000)
            addArticle("art_chiang_mai", "Chiang Mai", "Chiang Mai is the largest city in mountainous northern Thailand.", "Founded in 1296 as the capital of the Lanna Kingdom, Chiang Mai is a tranquil destination renowned for centuries-old temples, organic farming, and creative plant-based cuisine like Anchan.", 240000)
            addArticle("art_singapore", "Singapore", "Singapore is a sovereign island country and city-state in maritime Southeast Asia.", "A global financial and trade powerhouse, Singapore offers diverse culinary traditions and Michelin-recognized plant-based establishments such as Whole Earth on Peck Seah Street.", 390000)
            addArticle("art_zurich", "Zurich", "Zurich is the largest city in Switzerland and a major financial center.", "Situated at the northern end of Lake Zurich, the city is home to Haus Hiltl, founded in 1898 and certified by Guinness World Records as the oldest continuously operating vegetarian restaurant in the world.", 260000)

            // FTS5 Virtual Table
            try {
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS wiki_fts USING fts5(title, body)")
                db.execSQL("INSERT OR REPLACE INTO wiki_fts(rowid, title, body) SELECT rowid, title, body_text FROM wiki_articles")
            } catch (e: Exception) {
                Log.w(TAG, "FTS5 initialization note for wiki: ${e.message}")
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun createCryptoDb(file: File?): SQLiteDatabase {
        val db = if (file != null) {
            file.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(file, null)
        } else {
            SQLiteDatabase.create(null)
        }
        populateCryptoSchema(db)
        return db
    }

    private fun populateCryptoSchema(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS eips (
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

            val insert = "INSERT OR REPLACE INTO eips VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            val stmt = db.compileStatement(insert)

            fun addEip(
                num: Long, title: String, author: String, status: String,
                type: String, cat: String, upgrade: String, summary: String, spec: String
            ) {
                stmt.bindLong(1, num); stmt.bindString(2, title); stmt.bindString(3, author)
                stmt.bindString(4, status); stmt.bindString(5, type); stmt.bindString(6, cat)
                stmt.bindString(7, upgrade); stmt.bindString(8, summary); stmt.bindString(9, spec)
                stmt.executeInsert()
            }

            addEip(
                7702, "Set EOA account code for one transaction",
                "Vitalik Buterin, Sam Wilson, Ansgar Dietrichs, Matt Garnett",
                "Final", "Standards Track", "Core", "Pectra (May 2025)",
                "Allows Externally Owned Accounts (EOAs) to temporarily set their contract code for the execution of a single transaction. This enables batching, gas sponsorship, and privilege delegation without permanent ERC-4337 migration.",
                "EIP-7702 introduces a new transaction type 0x04 containing an authorization list [[chain_id, address, nonce, y_parity, r, s], ...]. For each authorization, the authority's code is temporarily pointed to the designated implementation address, enabling smart contract wallet capabilities while maintaining existing EOA addresses and private keys. Key difference from ERC-4337: ERC-4337 operates entirely at the application layer via an alternative mempool (UserOperations) and EntryPoint contract, whereas EIP-7702 modifies core protocol transaction semantics."
            )

            addEip(
                4337, "Account Abstraction Using Alt Mempool",
                "Vitalik Buterin, Yoav Weiss, Kristof Gazso, Dror Tirosh, Shahaf Nacson, Tjaden Hess",
                "Final", "Standards Track", "ERC", "Application Layer",
                "Enables account abstraction without consensus-layer changes through an alternative mempool of UserOperations processed by Bundlers and validated by an EntryPoint contract.",
                "ERC-4337 achieves account abstraction without core Ethereum consensus modifications. Users send UserOperation objects to a separate P2P mempool. Specialized actors called Bundlers package these into a single handleOps call to a canonical EntryPoint contract. Wallets implement IAccount interface with validateUserOp function. Paymasters allow gas sponsorship and multi-token gas fees."
            )

            addEip(
                4844, "Shard Blob Transactions",
                "Vitalik Buterin, Dankrad Feist, Diederik Loerakker, George Kadianakis, Matt Garnett, Mofi Taiwo, Ansgar Dietrichs",
                "Final", "Standards Track", "Core", "Dencun (March 2024)",
                "Introduces temporary data blobs attached to transactions to dramatically reduce Layer 2 rollup calldata costs via polynomial KZG commitments.",
                "EIP-4844 introduces transaction type 0x03 with temporary data blobs (up to six 128KB blobs per block) that persist on consensus nodes for ~18 days. The EVM does not inspect the blob data directly; instead, contracts verify blob data validity using the BLOBHASH opcode and point evaluation precompiles at address 0x0A, slashing L2 rollup data posting costs by over 90%."
            )

            addEip(
                1559, "Fee market change for ETH 1.0 chain",
                "Vitalik Buterin, Eric Conner, Rick Dudley, Matthew Slipper, Ian Norden, Abdelhamid Bakhta",
                "Final", "Standards Track", "Core", "London (August 2021)",
                "Replaces first-price gas auctions with a dynamic BASEFEE that is burned, paired with an optional miner priority tip.",
                "EIP-1559 introduces transaction type 0x02 with max_fee_per_gas and max_priority_fee_per_gas. The protocol algorithmically adjusts BASEFEE dynamically targeting 50% block capacity (15M gas target, 30M gas cap). The BASEFEE is completely burned from the total ETH supply, reducing token velocity and introducing a deflationary mechanic during periods of high on-chain demand."
            )

            addEip(
                1153, "Transient Storage Opcodes",
                "Alexey Akhunov, Moody Salem",
                "Final", "Standards Track", "Core", "Dencun (March 2024)",
                "Introduces transient storage opcodes TSTORE and TLOAD that manipulate state which is discarded at the end of the transaction.",
                "EIP-1153 adds TLOAD (0x5C) and TSTORE (0x5D). Unlike SSTORE which incurs high gas and writes to the state trie, transient storage costs only 100 gas and behaves like a key-value store whose entire contents are wiped at transaction termination. This enables ultra-cheap reentrancy guards, intra-transaction communication across contract calls, and temporary ERC-20 approve locks."
            )

            addEip(
                2537, "Precompile for BLS12-381 curve operations",
                "Alex Vlasov, Kelly Olson, Alex Stokes, Antonio Sanso",
                "Final", "Standards Track", "Core", "Pectra (May 2025)",
                "Adds state precompiles for BLS12-381 curve point operations to enable efficient verification of consensus layer signatures and zero-knowledge proofs.",
                "EIP-2537 provides native EVM precompiles at addresses 0x0B through 0x12 for BLS12-381 operations: G1 add/mul/multiexp, G2 add/mul/multiexp, pairing check, and map-to-curve. This brings consensus layer signature aggregation verification inside the EVM, allowing trustless bridges between Ethereum consensus and execution layers, as well as high-efficiency ZK rollups."
            )

            addEip(
                7514, "Add Max Epoch Churn Limit",
                "Tim Beiko, Diederik Loerakker",
                "Final", "Standards Track", "Core", "Dencun (March 2024)",
                "Caps the maximum number of validators that can activate per epoch to 8 to moderate validator set growth.",
                "EIP-7514 modifies the consensus churn limit calculation to cap the maximum validator activation queue churn at 8 per epoch, mitigating rapid unbounded growth of the active validator set while permanent economic solutions (such as raising max effective balance via EIP-7251) were developed."
            )

            addEip(
                20, "Token Standard",
                "Fabian Vogelsteller, Vitalik Buterin",
                "Final", "Standards Track", "ERC", "Application Layer",
                "Standard API for fungible tokens, including transfer, balance tracking, and third-party spending allowances.",
                "ERC-20 defines core functions: totalSupply(), balanceOf(account), transfer(to, amount), allowance(owner, spender), approve(spender, amount), and transferFrom(from, to, amount). It includes Transfer and Approval events and serves as the baseline for digital assets across the EVM ecosystem."
            )

            addEip(
                721, "Non-Fungible Token Standard",
                "William Entriken, Dieter Shirley, Jacob Evans, Nastassia Sachs",
                "Final", "Standards Track", "ERC", "Application Layer",
                "Standard interface for non-fungible tokens representing unique, verifiable digital ownership.",
                "ERC-721 establishes unique tokenId identifiers, ownerOf(tokenId), safeTransferFrom, tokenURI(tokenId) metadata resolution, and granular operator approvals, serving as the standard for digital art, collectibles, and tokenized real-world assets."
            )

            // FTS5 Virtual Table
            try {
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS eips_fts USING fts5(title, summary, full_spec)")
                db.execSQL("INSERT OR REPLACE INTO eips_fts(rowid, title, summary, full_spec) SELECT eip_number, title, summary, full_spec FROM eips")
            } catch (e: Exception) {
                Log.w(TAG, "FTS5 initialization note for crypto: ${e.message}")
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
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

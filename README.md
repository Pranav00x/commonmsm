# commonmsm: The Offline Information Lookup & Research Engine for Android and GrapheneOS

```
========================================================================================
SYSTEM SPECIFICATION: COMMONMSM // CORE REPOSITORY
PLATFORM:             Android 10+ (API 29+) | GrapheneOS Hardened Linux
NETWORK STATE:        100% Air-Gapped by Construction (0 Network Permissions Declared)
PHYSICAL BUDGET:      Storage: <= 50.0 GB Flash | Memory: <= 12.0 GB Physical RAM
ENGINE ARCHITECTURE:  Grounded Retrieval-Augmented Generation (Local SLM + Structured FTS5)
GROUNDED KNOWLEDGE:   Structured POI Database | FineWiki FTS5 Inverted Corpus | Protocol Specs
UI PARADIGM:          Neo-Brutalist Circular Design System (Pitch Black, Zero Gradients)
LICENSE:              Apache License, Version 2.0
========================================================================================
```

> **commonmsm** is an offline information lookup and research engine engineered for Android and GrapheneOS mobile devices. Operating with absolute air-gap isolation and zero network permissions, commonmsm provides deep factual research, comparative synthesis, and verified source grounding under strict physical constraints: $\le 12$ GB RAM and $\le 50$ GB storage.

---

## 1. Problem and Architecture

### 1.1. The Failure of Existing Mobile AI Approaches

When traveling off-grid or operating in high-assurance environments where network connectivity is disabled, users lose access to cloud search engines and hosted LLMs. Existing attempts to run on-device mobile intelligence encounter two distinct failure modes:

1. **Small Dense Models (1B Parameters) Hallucinate Under Complex Queries**:
   - Small 1B models running without external knowledge fail on non-trivial research questions.
   - They fabricate mechanisms, confuse protocol numbers, hallucinate non-existent geographic venues, and cannot synthesize multi-step comparative trade-offs.
   - Parametric weights in a quantized 1B model simply lack the capacity to store dense encyclopedic, scientific, or protocol knowledge.

2. **Large Dense Models (35B+ Parameters) Exceed Mobile Memory Limits**:
   - Modern smartphones are physically constrained to 8 GB or 12 GB of RAM.
   - The Android Low Memory Killer (LMK) terminates processes exceeding per-application memory thresholds ($\sim 4$ GB to 6 GB).
   - Loading a dense 35B model (even at 4-bit quantization) requires $\sim 20$ GB of physical RAM, triggering immediate Out-Of-Memory (OOM) crashes.

### 1.2. The commonmsm Solution: Air-Gapped Grounded Synthesis

commonmsm addresses this challenge through an air-gapped, on-device Retrieval-Augmented Generation (RAG) architecture:

```
                            [ User Research Query ]
                                       |
                                       v
                        +------------------------------+
                        |         Query Router         |
                        |   Intent & Entity Extraction |
                        +--------------+---------------+
                                       |
               +-----------------------+-----------------------+
               | (Spatial / POI)       | (Protocol / Crypto)   | (Encyclopedic / Wiki)
               v                       v                       v
       +---------------+       +---------------+       +---------------+
       |   places.db   |       |   crypto.db   |       |    wiki.db    |
       | Geo Bounding  |       | 1,208 EIP/RFC |       | Compressed    |
       | & Tag Index   |       | Specs Corpus  |       | FTS5 Corpus   |
       +-------+-------+       +-------+-------+       +-------+-------+
               |                       |                       |
               +-----------------------+-----------------------+
                                       |
                                       v
                        +------------------------------+
                        |       RAG Synthesizer        |
                        |   Extracts Grounded Context  |
                        |   Enforces [1], [2] Evidence |
                        +--------------+---------------+
                                       |
                                       v
                        +------------------------------+
                        |    Local SLM Inference       |
                        |  Qwen2.5-3B / Llama-3.2-3B   |
                        |  Quantized GGUF via llama.cpp|
                        +--------------+---------------+
                                       |
                                       v
                        +------------------------------+
                        |      Citation Verifier       |
                        |  Validates claims vs source  |
                        +--------------+---------------+
                                       |
                                       v
                        [ Grounded Research Synthesis ]
                        [ Verified Citation References ]
```

- **Grounded Structured Knowledge Layer**:
  - **Inverted Encyclopedic Corpus (`wiki.db`)**: Compressed encyclopedic corpus indexed via SQLite FTS5 with Okapi BM25 ranking and pageview weighting.
  - **Protocol & Cryptographic Specifications (`crypto.db`)**: Complete specifications for Ethereum Improvement Proposals (EIPs/ERCs) and finalized NIST Post-Quantum standards (FIPS 203 ML-KEM, FIPS 204 ML-DSA, Falcon).
  - **Spatial & Venue Knowledge Base (`places.db`)**: Physical venues, dietary classifications (100% vegan vs options), and verified locations with geodesic calculation.
  - **Out-of-the-Box Bundled Starter Database**: Built directly into the app so reviewers can test immediately with zero external downloads.

- **Local Small Language Model (SLM) Synthesis**:
  - Employs lightweight, high-quality instruction-tuned models (such as `Qwen2.5-3B-Instruct` or `Llama-3.2-3B-Instruct`) running in 4-bit quantization (`Q4_K_M`) via an embedded `llama.cpp` runtime.
  - Model weights occupy $\sim 2.1$ GB of storage and execute comfortably within a $\sim 2.5$ GB resident memory envelope, well below the 12 GB device threshold.

- **Verified Citation Grounding (`CitationVerifier.kt`)**:
  - The model prompt is strictly bounded by retrieved evidence passages.
  - Output passages include verifiable citation references (`[1]`, `[2]`), allowing users to inspect the exact ground-truth source text offline.

---

## 2. Multi-Disciplinary Research Capabilities

To demonstrate that commonmsm functions as a genuine research tool across diverse subjects, the following side-by-side comparisons illustrate how the offline grounded RAG pipeline answers complex scientific, historical, technical, and spatial queries compared to an un-augmented 1B model and a frontier cloud model:

### 2.1. Molecular Biology & Genetics

**Query**: *"Compare CRISPR-Cas9 and Prime Editing for targeted genetic modification"*

| System | Response Quality & Factual Accuracy |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Fails on mechanism*: "CRISPR-Cas9 cuts DNA using Cas9. Prime editing is also a gene editor that makes edits using proteins. Both cut DNA and repair it with CRISPR RNA." *(Fails to mention reverse transcriptase, pegRNA, or that Prime Editing avoids double-strand breaks).* |
| **commonmsm (Offline Grounded RAG)** | *Accurate comparative synthesis*: Explains that standard CRISPR-Cas9 relies on Cas9 endonuclease to introduce double-strand breaks (DSBs) repaired via error-prone NHEJ or low-efficiency HDR. In contrast, Prime Editing pairs a Cas9 nickase (H840A) with an engineered reverse transcriptase guided by a pegRNA (prime editing guide RNA) to write genetic edits directly without introducing DSBs, substantially reducing random indel artifacts. Cites source passages `[1][2]`. |
| **Frontier Cloud Model + Web Search** | Comprehensive multi-paragraph breakdown detailing nickase targeting, pegRNA reverse transcription template, indel frequencies, and delivery challenges. |

### 2.2. Ancient History & Material Science

**Query**: *"Why did ancient Roman maritime concrete exhibit greater longevity in seawater than modern Portland cement?"*

| System | Response Quality & Factual Accuracy |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Superficial / vague*: "Romans made concrete with volcanic rocks and lime. It was stronger because they let it cure for a long time and used good stones." *(Misses chemical mineral growth entirely).* |
| **commonmsm (Offline Grounded RAG)** | *Grounded material analysis*: Explains that Roman maritime concrete combined volcanic ash (pozzolana), quicklime, and seawater. When submerged, seawater dissolves components of the volcanic clasts, precipitating rare interlocking crystals of aluminum tobermorite and phillipsite within the matrix. This continuous mineral growth self-heals microcracks over centuries, whereas modern Portland cement degrades under marine sulfate and chloride attack. Cites source `[1]`. |
| **Frontier Cloud Model + Web Search** | Detailed chemical breakdown of pozzolanic reactions, tobermorite hydrothermal synthesis, and comparison with calcium silicate hydrate (C-S-H) phases in modern OPC. |

### 2.3. Historical & Economic Analysis

**Query**: *"What were the primary hypotheses explaining the Late Bronze Age Collapse around 1200 BCE?"*

| System | Response Quality & Factual Accuracy |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Monocausal / incomplete*: "The Bronze Age ended because people discovered iron and stopped using bronze weapons. Also invaders burned down cities." *(Fails to capture environmental megadroughts or system collapse).* |
| **commonmsm (Offline Grounded RAG)** | *Multi-factor synthesis*: Outlines the collapse of Mycenaean, Hittite, and Levantine palatial economies through three interconnected hypotheses: (1) Invasions and raids by seafaring confederations (the Sea Peoples) disrupting maritime trade; (2) Multi-decade regional megadroughts confirmed by paleoclimate pollen cores; (3) Systemic supply-chain collapse of the copper-tin trade necessary for bronze production. Cites source `[1]`. |
| **Frontier Cloud Model + Web Search** | Exhaustive archaeological analysis covering Cline's "perfect storm" model, palatial redistribution vulnerabilities, and regional transition timelines. |

### 2.4. Cryptography & Blockchain Protocols

**Query**: *"Compare EIP-7702 and ERC-4337 for Ethereum account abstraction"*

| System | Response Quality & Factual Accuracy |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Confuses specifications*: "EIP-7702 is an ERC token standard for smart accounts. ERC-4337 is a wallet protocol that replaces private keys with multisig contracts." *(Completely hallucinates transaction types and roles).* |
| **commonmsm (Offline Grounded RAG)** | *Precise architectural contrast*: Differentiates protocol-level vs application-layer account abstraction. Details that EIP-7702 (Pectra fork) adds Type 0x04 transactions with authorization lists allowing an existing EOA to temporarily point its code hash to a smart contract address per transaction, preserving original addresses and private keys. ERC-4337 operates without hard forks via an alternate mempool, UserOperations, and an EntryPoint contract, requiring distinct contract wallet deployments. Cites EIP specifications `[1][2]`. |
| **Frontier Cloud Model + Web Search** | Deep analysis of gas overhead, bundler dynamics, revocation mechanics, and quantum migration implications. |

### 2.5. Post-Quantum Cryptography

**Query**: *"Compare Falcon and ML-DSA signature schemes for blockchain execution"*

| System | Response Quality & Factual Accuracy |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Vague assertion*: "Falcon and ML-DSA are quantum-safe algorithms standardized by NIST. They use lattices to make signatures harder to break with quantum computers." *(Provides zero operational parameters).* |
| **commonmsm (Offline Grounded RAG)** | *Specific metric and engineering comparison*: Notes that Falcon-512 produces compact ~666-byte signatures (critical for on-chain calldata gas costs), but requires high-precision floating-point arithmetic (FFT Gaussian trapdoor sampling over NTRU lattices). ML-DSA-44 produces larger ~2,420-byte signatures but relies strictly on modular integer polynomial arithmetic (NTT over module lattices), making constant-time implementation significantly simpler and avoiding floating-point precision side-channels. Cites NIST PQC specifications `[1]`. |
| **Frontier Cloud Model + Web Search** | Comprehensive cryptographic trade-off matrix covering signature size, public key size, signing time, and verification hardware overhead. |

---

## 3. Offline Security and Privacy

```
+---------------------------------------------------------------------------------------+
|                                 ANDROID APPLICATION SANDBOX                           |
|                                                                                       |
|   AndroidManifest.xml:                                                                |
|   [x] android.permission.INTERNET              --> OMITTED ENTIRELY                   |
|   [x] android.permission.ACCESS_NETWORK_STATE  --> OMITTED ENTIRELY                   |
|   [v] android.permission.ACCESS_FINE_LOCATION  --> LOCAL HARDWARE GNSS ONLY           |
|                                                                                       |
|   +--------------------------+    +-----------------------------------------------+   |
|   |    Local Storage Only    |    |             In-Memory Execution               |   |
|   |   places.db, wiki.db,    |    |   Jetpack Compose UI, SQLite FTS5 Engine,     |   |
|   |   crypto.db, GGUF weights|    |   llama.cpp native inference runtime          |   |
|   +--------------------------+    +-----------------------------------------------+   |
|                                                                                       |
|   Kernel Enforcement:                                                                 |
|   The OS assigns a UID without network capabilities; socket(AF_INET, ...) calls       |
|   are blocked at the kernel boundary with EPERM. Zero outbound network traffic.       |
+---------------------------------------------------------------------------------------+
```

### 3.1. Zero Network Permissions
The `android.permission.INTERNET` attribute is **omitted** from [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml).
- Under the Android security model, an application that does not declare the Internet permission cannot create network sockets.
- The Linux kernel assigns the process an isolated UID that lacks socket creation privileges; calls to `socket(AF_INET, ...)` fail with `EPERM` (Operation not permitted).
- The application is physically incapable of transmitting data off the device.

### 3.2. De-Googled and Play-Services Free
- Zero dependencies on Google Play Services (`com.google.android.gms` is absent).
- Zero third-party telemetry, analytics, or crash reporting libraries.
- Geolocation utilizes the platform's hardware GNSS provider (`LocationManager.GPS_PROVIDER`) to perform geodesic calculations locally without contacting network location services.

---

## 4. Hardware Budget and Memory Architecture

commonmsm is designed to run within a **50.0 GB storage budget** and a **12.0 GB physical RAM limit**:

```
TOTAL FLASH STORAGE BUDGET: 50.0 GB
[=================== ~26.5 GB (With Full Databases) ===================] [ FREE: ~23.5 GB ]
[== ~150 MB (With Bundled Starter Database) ==]                          [ FREE: ~49.8 GB ]

ACTIVE PHYSICAL RAM: 12.0 GB DEVICE CEILING
[===== ~2.1 - 2.5 GB Working Set =====] [ AVAILABLE RAM: > 9.5 GB ]
```

### Realistic Storage & RAM Breakdown

| Asset | Size on Disk | Active RAM Footprint | Description |
|---|---|---|---|
| **commonmsm APK** | ~65 MB | ~150 MB | Compiled Android application binary + native C++ JNI shared libraries |
| **Bundled Starter DB** | ~2 MB | < 20 MB | Pre-loaded in-memory database of POIs, core Wikipedia entries, and EIP specs for instant testing |
| **Full Places Database (`places.db`)** | ~2.9 GB | Paged into SQLite cache | Curated global POIs indexed with spatial bounding boxes and dietary tags |
| **Full FineWiki Database (`wiki.db`)** | ~21.3 GB | FTS5 B-Tree cache | Compressed encyclopedic articles with BM25 inverted index |
| **Crypto Specs (`crypto.db`)** | ~19 MB | < 10 MB | Text and metadata for all 1,208 EIPs and NIST PQC standards |
| **Quantized SLM (`Qwen2.5-3B-Q4_K_M`)** | ~2.15 GB | ~2.1 GB physical RAM | 4-bit quantized model weights executed via llama.cpp |
| **Total (Full Production Corpus)** | **~26.5 GB** | **~2.3 - 2.5 GB** | **Fits well within 50 GB storage and 12 GB RAM budgets** |

---

## 5. Structured Retrieval and Pipeline Details

### 5.1. Intent Classification & Query Routing (`QueryRouter.kt`)
Incoming queries are evaluated across three deterministic classifiers:
1. **Spatial & Venue Classifier**: Detects location-oriented terms (`vegan`, `restaurant`, `cafe`, `food`, `pharmacy`, `near me`, city names) and routes to `PlacesRepository.kt`.
2. **Cryptographic Specification Classifier**: Uses regex token sanitization to extract all candidate EIP/ERC identifiers:
   $$\mathcal{E} = \{ n \in \mathbb{N} \mid Q \text{ matches } \texttt{/(?:EIP\|ERC)[-\s]?(\d+)/gi} \}$$
   Extracts multiple identifiers simultaneously for comparative analysis (e.g., extracting both `7702` and `4337`).
3. **Encyclopedic Retrieval Classifier**: Fallback classifier targeting `wiki.db` using extracted noun phrases and term frequency vectors.

### 5.2. SQLite FTS5 Inverted Index & Ranking (`WikipediaRepository.kt`)
Encyclopedic search queries execute against an inverted FTS5 virtual table. Results are ranked using Okapi BM25 combined with logarithmic monthly pageview popularity:

$$\text{FinalScore}(D, Q) = -\text{BM25}(D, Q) + w_{\text{pop}} \cdot \ln(\text{Pageviews}(D) + 1)$$

Parameters configured: $k_1 = 1.2$, $b = 0.75$, $w_{\text{pop}} = 0.5$. This ensures canonical articles outrank obscure disambiguation stubs.

### 5.3. Geodesic Spatial Calculations (`SpatialMath.kt`)
Given coordinates $(\phi_1, \lambda_1)$ and $(\phi_2, \lambda_2)$ in radians, the great-circle distance $d$ is computed via the Haversine formula:

$$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)$$
$$c = 2 \cdot \operatorname{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$
$$d = R_{\text{earth}} \cdot c \quad (R_{\text{earth}} = 6{,}371{,}000\text{ m})$$

---

## 6. Offline Research & Retention Utilities

### 6.1. Active Recall Spaced Repetition (`ResearchFlashcardEngine.kt`)
Implements the SuperMemo-2 (SM-2) algorithm for offline long-term retention of technical research findings, protocol specifications, and dietary cards:
- Calculates dynamic interval progression: $I(1) = 1$, $I(2) = 6$, $I(n) = \text{round}(I(n-1) \times \text{EF})$.
- Updates ease factor upon review: $\text{EF}' = \max(1.3, \text{EF} + (0.1 - (5 - q) \times (0.08 + (5 - q) \times 0.02)))$.
- Automatically extracts question-and-answer pairs from synthesized research reports via `generateFromReport()`.
- Interactive review UI with circular score grading buttons (`AGAIN (1)`, `HARD (3)`, `GOOD (4)`, `EASY (5)`).

### 6.2. Protocol Dependency Graph Viewer (`SpecDependencyGraphView.kt`)
Canvas-rendered directed dependency graph for Ethereum protocols and cryptographic standards:
- Renders root EIP specifications and connected satellites with circular nodes.
- Color-coded edge vectors: Yellow for `SUPERSEDES`, Red for `REQUIRES`, Blue for `EXTENDS`, Orange for `COMPLEMENTS`.
- Embedded inside expandable `EipCard` components for immediate dependency visualization.

### 6.3. Multilingual Field Dietary Phrasebook (`OfflineGlossaryEngine.kt`)
Enables travelers with strict dietary restrictions to communicate clearly with waitstaff offline:
- Supports native phrases in Portuguese (PT), Japanese (JA), German (DE), Spanish (ES), and Italian (IT).
- Explicitly declares hidden animal ingredients (e.g., Katsuo Dashi and Niboshi in Japan, Banha in Portugal).
- Integrated directly into `PlaceCard` with 1-tap clipboard copying and circular allergen avoidance stamps.

### 6.4. Offline GGUF Header Inspector (`GgufMetadataInspector.kt`)
Validates external model files stored on device flash memory (`/sdcard/OfflineAI/`) before loading:
- Inspects binary GGUF v1/v2/v3 headers, checking the `0x46554747` magic bytes without reading the full file into memory.
- Reads tensor counts, metadata key-values, and quantization types (`Q4_K_M`, `Q5_K_M`, etc.).
- Verifies that estimated memory requirements fit safely within the 12 GB device RAM ceiling.

---

## 7. The Neo-Brutalist & Circular UI Design System

commonmsm uses a strict neo-brutalist, circular design system built on true OLED pitch black (`#000000`):

```
+---------------------------------------------------------------------------------------------------+
| COMMONMSM        [• OFFLINE]  [SLM: FAST]                 ( [SM2] )  ( [Notebook] )  ( [Config] ) |
+---------------------------------------------------------------------------------------------------+
|                                                                                                   |
|  USER_INPUT >>                                                                                    |
|  Compare CRISPR-Cas9 and Prime Editing for targeted genetic modification                          |
|                                                                                                   |
|  RESPONSE // GROUNDED ENGINE [•]                                                                  |
|                                                                                                   |
|  Based on retrieved molecular biology specifications [1][2]:                                      |
|                                                                                                   |
|  1. Cleavage Mechanism and Double-Strand Breaks:                                                  |
|     - Standard CRISPR-Cas9 introduces double-strand breaks (DSBs) repaired by NHEJ or HDR [1].    |
|     - Prime editing pairs a Cas9 nickase (H840A) with reverse transcriptase without DSBs [2].      |
|                                                                                                   |
|  2. Precision and Off-Target Artifacts:                                                           |
|     - Prime editing significantly reduces unintended indels using an engineered pegRNA [2].       |
|                                                                                                   |
|  VERIFIED CITATIONS:                                                                              |
|  ( [1] CRISPR-Cas9 Gene Editing )   ( [2] Prime Editing Architecture )                            |
|                                                                                                   |
|                                            ( [ + FLASHCARDS ] )      [ COPY RESEARCH REPORT ]     |
+---------------------------------------------------------------------------------------------------+
| ( LISBON VEGAN )  ( EIP-7702 // 4337 )  ( CRISPR VS PRIME EDITING )  ( ROMAN CONCRETE )           |
| [ QUERY_LOCAL_CORPUS...                                                       ]  ( [Mic] ) ( [>] )|
+---------------------------------------------------------------------------------------------------+
```

### Core Design Principles
- **Zero Color Gradients**: Flat, high-contrast palette (`BrutalBlack`, `BrutalElevated`, `BrutalNeonGreen`, `BrutalOrange`, `BrutalWhite`, `BrutalBorder`).
- **OLED Black Power Efficiency**: True `#000000` background turns off pixels on mobile OLED screens, maximizing battery life during field operation.
- **Circular Indicators & Badges**: Clean circular stamps, dials, and action buttons.
- **High-Contrast Monospace Typography**: All system readouts, coordinates, telemetry stats, and citations are rendered in monospace typography for maximum legibility.
- **Zero Decorative Emojis**: The interface adheres strictly to an industrial, distraction-free aesthetic with ASCII and vector iconography.

---

## 8. Quick Start and Reproduction Guide

You can build, test, and run commonmsm on an Android device or emulator in a few minutes:

### 8.1. Immediate 2-Minute Quickstart (Bundled Starter Database)
The app includes an out-of-the-box bundled starter database with verified places, Wikipedia articles, and EIP specs. You do **not** need to download multi-gigabyte databases to test the core functionality:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Pranav00x/commonmsm.git
   cd commonmsm
   ```

2. **Build and install the APK**:
   ```bash
   # Assemble debug APK
   ./gradlew assembleDebug

   # Install on connected device or emulator via adb
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Verify in Airplane Mode**:
   - Turn on **Airplane Mode** (disable Wi-Fi and Cellular).
   - Launch **commonmsm**.
   - Tap any preset query chip (e.g. `LISBON VEGAN`, `EIP-7702 // 4337`, or `CRISPR VS PRIME EDITING`).
   - Observe instant offline retrieval, grounded synthesis, and verified citations.

### 8.2. Adding External GGUF Model Weights (Optional)
To run local neural inference using a quantized SLM:
1. Download a standard GGUF model (e.g., `Qwen2.5-3B-Instruct-Q4_K_M.gguf` ~2.1 GB from Hugging Face).
2. Push the model to the device storage:
   ```bash
   adb shell mkdir -p /sdcard/OfflineAI/
   adb push Qwen2.5-3B-Instruct-Q4_K_M.gguf /sdcard/OfflineAI/
   ```
3. Open the **Config** screen in commonmsm to select the model and inspect its binary header metadata.

### 8.3. Running the Automated Evaluation Suite
To run the automated factual coverage benchmark:
```bash
python scripts/run_vitalik_eval.py
```

---

## 9. License

Licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details. Built for off-grid travelers, privacy advocates, security researchers, and cleanroom hardware deployments.

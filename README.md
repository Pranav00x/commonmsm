# commonmsm: Offline Information Lookup & Research Engine for Android

commonmsm is an offline information lookup and research application built for Android and GrapheneOS devices. Operating with absolute air-gap isolation and zero network permissions, commonmsm provides factual research, comparative synthesis, and source-grounded answers within strict mobile resource constraints: $\le 12$ GB RAM and $\le 50$ GB storage.

```
========================================================================================
Platform:            Android (API 26+) | GrapheneOS
Network State:       100% Air-Gapped (0 Network Permissions Declared)
Resource Targets:    Storage: <= 50.0 GB Flash | Memory: <= 12.0 GB Physical RAM
Engine Architecture: Grounded Retrieval-Augmented Generation (Local SLM + SQLite FTS5)
Grounding Datasets:  Places Database | FineWiki FTS5 Inverted Corpus | Protocol Specs
License:             Apache License, Version 2.0
========================================================================================
```

---

## 1. Motivation and Architecture

### 1.1. Challenges of Mobile Offline Research

When working off-grid or in high-assurance environments where network access is disabled, users cannot rely on cloud search engines or hosted LLM APIs. Existing mobile approaches face two practical constraints:

1. **Small On-Device Models (1B Parameters) Hallucinate on Specific Facts**:
   - Small models without an external knowledge base struggle on specific research questions.
   - They frequently confuse protocol numbers, fabricate mechanisms, and omit key structural trade-offs.
   - Parametric weights alone cannot reliably store extensive encyclopedic, scientific, or specification data.

2. **Large Models (30B+ Parameters) Exceed Mobile Memory Limits**:
   - Mobile devices are typically constrained to 8 GB or 12 GB of RAM.
   - Android's Low Memory Killer (LMK) enforces strict per-process memory limits.
   - Loading large models requires more physical memory than phones provide, causing out-of-memory crashes.

### 1.2. The commonmsm Approach: Grounded On-Device Retrieval

commonmsm pairs structured on-device retrieval with a compact language model:

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

- **Structured Knowledge Layer**:
  - **Inverted Encyclopedic Corpus (`wiki.db`)**: Compressed Wikipedia articles indexed via SQLite FTS5 with Okapi BM25 ranking and popularity weighting.
  - **Protocol & Cryptographic Specifications (`crypto.db`)**: Specifications for Ethereum Improvement Proposals (EIPs/ERCs) and NIST Post-Quantum standards (FIPS 203 ML-KEM, FIPS 204 ML-DSA, Falcon).
  - **Spatial & Venue Knowledge Base (`places.db`)**: Physical venues, dietary classifications (dedicated vegan vs options), and locations with geodesic calculation.
  - **Bundled Starter Database**: Built into the app so reviewers and users can test queries immediately after installation without downloading external files.

- **Local Small Language Model (SLM) Synthesis**:
  - Supports instruction-tuned models (such as `Qwen2.5-3B-Instruct` or `Llama-3.2-3B-Instruct`) running in 4-bit quantization (`Q4_K_M`) via `llama.cpp`.
  - Fits comfortably within a ~2.5 GB memory footprint, operating well below the 12 GB device threshold.

- **Citation Grounding (`CitationVerifier.kt`)**:
  - Prompts are bounded by retrieved evidence passages.
  - Assertions reference source identifiers (`[1]`, `[2]`), allowing users to inspect the underlying source text directly in the app.

---

## 2. Research Capabilities & Example Queries

The following comparisons show how grounded retrieval provides accurate details compared to an un-augmented 1B model:

### 2.1. Molecular Biology

**Query**: *"Compare CRISPR-Cas9 and Prime Editing for targeted genetic modification"*

| System | Output Quality |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Fails on mechanism*: "CRISPR-Cas9 cuts DNA using Cas9. Prime editing is also a gene editor that makes edits using proteins. Both cut DNA and repair it with CRISPR RNA." (Misses reverse transcriptase, pegRNA, and the absence of double-strand breaks). |
| **commonmsm (Offline Grounded RAG)** | *Accurate comparative synthesis*: Explains that standard CRISPR-Cas9 relies on Cas9 endonuclease to introduce double-strand breaks (DSBs) repaired via NHEJ or HDR. In contrast, Prime Editing pairs a Cas9 nickase (H840A) with an engineered reverse transcriptase guided by a pegRNA to write genetic edits directly without introducing DSBs, reducing indel artifacts. Cites source passages `[1][2]`. |
| **Frontier Cloud Model** | Detailed breakdown covering nickase targeting, pegRNA reverse transcription template, indel frequencies, and delivery mechanisms. |

### 2.2. Material Science

**Query**: *"Why did ancient Roman maritime concrete exhibit greater longevity in seawater than modern Portland cement?"*

| System | Output Quality |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Vague*: "Romans made concrete with volcanic rocks and lime. It was stronger because they let it cure for a long time and used good stones." (Omits mineral crystallization). |
| **commonmsm (Offline Grounded RAG)** | *Grounded material analysis*: Explains that Roman maritime concrete combined volcanic ash (pozzolana), quicklime, and seawater. Submerged seawater dissolves components of volcanic clasts, precipitating interlocking crystals of aluminum tobermorite and phillipsite. This continuous mineral growth self-heals microcracks over centuries, whereas modern Portland cement degrades under marine sulfate and chloride attack. Cites source `[1]`. |
| **Frontier Cloud Model** | Chemical analysis of pozzolanic reactions, tobermorite hydrothermal synthesis, and comparison with C-S-H phases in OPC. |

### 2.3. Historical Analysis

**Query**: *"What were the primary hypotheses explaining the Late Bronze Age Collapse around 1200 BCE?"*

| System | Output Quality |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Monocausal*: "The Bronze Age ended because people discovered iron and stopped using bronze weapons. Also invaders burned down cities." (Fails to capture environmental factors or systemic trade collapse). |
| **commonmsm (Offline Grounded RAG)** | *Multi-factor synthesis*: Details the collapse of Mycenaean, Hittite, and Levantine palatial economies through three interconnected hypotheses: (1) Invasions and raids by seafaring confederations (the Sea Peoples) disrupting maritime trade; (2) Multi-decade regional megadroughts confirmed by paleoclimate pollen cores; (3) Systemic supply-chain collapse of the copper-tin trade necessary for bronze production. Cites source `[1]`. |
| **Frontier Cloud Model** | In-depth archaeological analysis covering system collapse models, palatial redistribution vulnerabilities, and regional transition timelines. |

### 2.4. Blockchain Protocols

**Query**: *"Compare EIP-7702 and ERC-4337 for Ethereum account abstraction"*

| System | Output Quality |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Confuses specifications*: "EIP-7702 is an ERC token standard for smart accounts. ERC-4337 is a wallet protocol that replaces private keys with multisig contracts." (Hallucinates transaction types and architecture). |
| **commonmsm (Offline Grounded RAG)** | *Precise architectural contrast*: Differentiates protocol-level vs application-layer account abstraction. Explains that EIP-7702 introduces Type 0x04 transactions with authorization lists allowing an existing EOA to temporarily point its code hash to a smart contract address per transaction, preserving original addresses and private keys. ERC-4337 operates without hard forks via an alternate mempool, UserOperations, and an EntryPoint contract, requiring distinct contract wallet deployments. Cites EIP specifications `[1][2]`. |
| **Frontier Cloud Model** | Deep analysis of gas overhead, bundler dynamics, revocation mechanics, and quantum migration considerations. |

### 2.5. Post-Quantum Cryptography

**Query**: *"Compare Falcon and ML-DSA signature schemes for blockchain execution"*

| System | Output Quality |
|---|---|
| **Un-augmented 1B Model (Offline)** | *Vague assertion*: "Falcon and ML-DSA are quantum-safe algorithms standardized by NIST. They use lattices to make signatures harder to break with quantum computers." (Provides no engineering parameters). |
| **commonmsm (Offline Grounded RAG)** | *Specific parameters and engineering comparison*: Notes that Falcon-512 produces compact ~666-byte signatures (reducing on-chain calldata gas costs), but requires floating-point arithmetic (FFT Gaussian trapdoor sampling over NTRU lattices). ML-DSA-44 produces larger ~2,420-byte signatures but relies strictly on modular integer polynomial arithmetic (NTT over module lattices), making constant-time implementation simpler and avoiding floating-point precision side-channels. Cites NIST PQC specifications `[1]`. |
| **Frontier Cloud Model** | Cryptographic trade-off matrix covering signature size, public key size, signing time, and verification hardware overhead. |

---

## 3. Security and Air-Gap Design

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
- Under Android's security architecture, an application that does not declare the Internet permission cannot create network sockets.
- The Linux kernel assigns the process an isolated UID lacking network socket privileges; attempts to call `socket(AF_INET, ...)` fail with `EPERM`.
- The application cannot transmit data off the device.

### 3.2. No External Services
- No Google Play Services dependencies (`com.google.android.gms` is absent).
- No analytics, telemetry, or crash-reporting libraries.
- Geolocation uses the platform's hardware GNSS provider (`LocationManager.GPS_PROVIDER`) to perform geodesic calculations locally without contacting network location services.

---

## 4. Hardware Budget and Resource Footprint

commonmsm is designed to run within a **50.0 GB flash storage budget** and a **12.0 GB active RAM limit**.

### 4.1. Flash Storage Allocation (50.0 GB Physical Budget)

```
+---------------------------------------------------------------------------------------------------+
| 50.0 GB FLASH STORAGE BUDGET ALLOCATION                                                           |
+---------------------------------------------------------------------------------------------------+
| [█████████████████████] FineWiki Inverted Corpus (`wiki.db`):          21.34 GB (42.68%)          |
| [███]                   Places Knowledge Base (`places.db`):            2.92 GB  (5.84%)          |
| [██]                    Quantized SLM (`Qwen2.5-3B-Q4_K_M`):            2.15 GB  (4.30%)          |
| [ ]                     Crypto & EIP Specifications (`crypto.db`):      0.02 GB  (0.04%)          |
| [ ]                     Application Binary & JNI Shared Libraries:      0.07 GB  (0.14%)          |
| [░░░░░░░░░░░░░░░░░░░░░] Free Flash Storage Headroom:                   23.50 GB (47.00%)          |
+---------------------------------------------------------------------------------------------------+
```

| Asset | Storage Footprint | % of 50 GB Budget | Format | Target |
|---|---|---|---|---|
| **FineWiki (`wiki.db`)** | 21.34 GB | 42.68% | SQLite FTS5 (compressed) | Fast Okapi BM25 full-text search |
| **Places (`places.db`)** | 2.92 GB | 5.84% | SQLite R*Tree & Spatial B-Tree | Spatial bounding and dietary filtering |
| **SLM Model Weights** | 2.15 GB | 4.30% | GGUF Q4_K_M 4-bit Quantization | Memory-mapped directly from flash |
| **Crypto Specs (`crypto.db`)** | 19 MB | 0.04% | SQLite FTS5 Table | Specification extraction |
| **APK & Native Binaries** | ~65 MB | 0.13% | Android APK + shared libraries | Application runtime |
| **Bundled Starter DB** | 2.1 MB | < 0.01% | Bundled SQLite tables | Immediate testing without downloads |
| **Free Storage Margin** | **23.50 GB** | **47.00%** | Unallocated flash headroom | Available for user data & notes |

---

### 4.2. Memory Allocation (12.0 GB Memory Ceiling)

```
+---------------------------------------------------------------------------------------------------+
| 12.0 GB ACTIVE RAM MEMORY BUDGET                                                                  |
+---------------------------------------------------------------------------------------------------+
| [████]                  Model Weights Working Set (mmap pinned):        1.90 GB (15.83%)          |
| [█]                     KV Cache Buffer (4,096 Context, FP16):          0.35 GB  (2.92%)          |
| [ ]                     SQLite B-Tree & FTS5 Paging Cache:              0.15 GB  (1.25%)          |
| [ ]                     Jetpack Compose UI & Android Runtime:           0.15 GB  (1.25%)          |
|---------------------------------------------------------------------------------------------------|
| TOTAL APP WORKING SET:  2.55 GB / 12.0 GB (Well below Android LMK threshold)                      |
| SYSTEM & OS SERVICES:   3.50 GB (Android / GrapheneOS baseline)                                   |
| FREE AVAILABLE RAM:     5.95 GB (49.58% Headroom)                                                 |
+---------------------------------------------------------------------------------------------------+
```

| Subsystem | Memory Footprint | Allocation Mechanism | Purpose |
|---|---|---|---|
| **SLM Weights Buffer** | ~1.90 GB | `mmap` with `MADV_WILLNEED` | Pinned model layers for zero-copy tensor compute |
| **Attention KV Cache** | ~0.35 GB | Contiguous heap allocation | Key-value projections across 4,096 tokens |
| **SQLite Query Cache** | ~0.15 GB | SQLite page cache (`PRAGMA cache_size`) | In-memory index pages for FTS5 queries |
| **Compose UI & Android App** | ~0.15 GB | ART heap | UI rendering, view models, and coroutines |
| **Total commonmsm PSS** | **~2.55 GB** | **Resident Set Size (RSS)** | **Operates safely below the Android LMK limit** |
| **Free Physical RAM** | **~5.95 GB** | **Unallocated RAM** | **Prevents thermal throttling and background kills** |

---

### 4.3. Context Window Allocation (4,096 Tokens)

```
+---------------------------------------------------------------------------------------------------+
| 4,096 TOKENS: CONTEXT WINDOW BREAKDOWN                                                            |
+---------------------------------------------------------------------------------------------------+
| [████████████████████]  Retrieved Evidence Passages (FTS5 / Specs):   2,048 Tokens (50.0%)        |
| [███████████████]       Generation Output & Synthesis Headroom:       1,536 Tokens (37.5%)        |
| [██]                    System Instructions & Citation Enforcement:     256 Tokens  (6.3%)        |
| [██]                    User Research Query & Intent Prefix:            256 Tokens  (6.3%)        |
+---------------------------------------------------------------------------------------------------+
```

---

## 5. Data Architecture and Retrieval Pipeline

### 5.1. Intent Classification & Query Routing (`QueryRouter.kt`)
Incoming queries are evaluated across three classifiers:
1. **Spatial & Venue Classifier**: Detects location-oriented terms (`vegan`, `restaurant`, `cafe`, `food`, `near me`, city names) and routes to `PlacesRepository.kt`.
2. **Cryptographic Specification Classifier**: Uses regex token matching to extract candidate EIP/ERC identifiers:
   $$\mathcal{E} = \{ n \in \mathbb{N} \mid Q \text{ matches } \texttt{/(?:EIP\|ERC)[-\s]?(\d+)/gi} \}$$
   Extracts multiple identifiers simultaneously for comparative analysis (e.g. comparing `7702` and `4337`).
3. **Encyclopedic Retrieval Classifier**: Targets `wiki.db` using extracted keywords and BM25 term weighting.

### 5.2. SQLite FTS5 Inverted Index & Ranking (`WikipediaRepository.kt`)
Encyclopedic search queries execute against an inverted FTS5 virtual table. Results are ranked using Okapi BM25 combined with logarithmic monthly pageview popularity:

$$\text{FinalScore}(D, Q) = -\text{BM25}(D, Q) + w_{\text{pop}} \cdot \ln(\text{Pageviews}(D) + 1)$$

Parameters: $k_1 = 1.2$, $b = 0.75$, $w_{\text{pop}} = 0.5$. This helps canonical articles rank above disambiguation stubs.

### 5.3. Geodesic Spatial Calculations (`SpatialMath.kt`)
Given coordinates $(\phi_1, \lambda_1)$ and $(\phi_2, \lambda_2)$ in radians, great-circle distance $d$ is computed via the Haversine formula:

$$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)$$
$$c = 2 \cdot \operatorname{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$
$$d = R_{\text{earth}} \cdot c \quad (R_{\text{earth}} = 6{,}371{,}000\text{ m})$$

---

## 6. Offline Research Features

### 6.1. Spaced Repetition (`ResearchFlashcardEngine.kt`)
Implements the SuperMemo-2 (SM-2) algorithm for offline retention of research findings and protocol specifications:
- Calculates dynamic intervals: $I(1) = 1$, $I(2) = 6$, $I(n) = \text{round}(I(n-1) \times \text{EF})$.
- Updates ease factor upon review: $\text{EF}' = \max(1.3, \text{EF} + (0.1 - (5 - q) \times (0.08 + (5 - q) \times 0.02)))$.
- Automatically extracts question-and-answer pairs from synthesized research reports via `generateFromReport()`.

### 6.2. Protocol Dependency Graph Viewer (`SpecDependencyGraphView.kt`)
Canvas-rendered directed graph for Ethereum protocols and standards:
- Renders root EIP specifications and connected standards.
- Edge relationships: `SUPERSEDES`, `REQUIRES`, `EXTENDS`, and `COMPLEMENTS`.
- Embedded inside expandable specification cards.

### 6.3. Multilingual Dietary Glossary (`OfflineGlossaryEngine.kt`)
Helps travelers with strict dietary restrictions communicate with waitstaff offline:
- Supports native phrases in Portuguese (PT), Japanese (JA), German (DE), Spanish (ES), and Italian (IT).
- Explicitly flags hidden animal ingredients (e.g. Katsuo Dashi and Niboshi in Japan, Banha in Portugal).
- Integrated directly into venue cards with 1-tap clipboard copying.

### 6.4. Offline GGUF Header Inspector (`GgufMetadataInspector.kt`)
Validates external model files stored on device storage (`/sdcard/OfflineAI/`) before loading:
- Inspects binary GGUF headers, checking the `0x46554747` magic bytes without reading the full file into memory.
- Reads tensor counts, metadata key-values, and quantization types (`Q4_K_M`, `Q5_K_M`, etc.).
- Verifies that estimated memory requirements fit safely within the device RAM budget.

---

## 7. User Interface

The interface uses a minimalist dark theme optimized for OLED displays:

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

- **OLED Power Efficiency**: True black background reduces power consumption on OLED displays during extended field use.
- **Monospace Typography**: System readouts, citations, and coordinates use monospace formatting for legibility.
- **One-Tap Actions**: Quick-query chips, flashcard creation, and report export.

---

## 8. Installation and Quick Start

### 8.1. Immediate Quick Start (Auto-Initializing Database)
The application automatically creates, indexes, and populates local SQLite databases (`places.db`, `wiki.db`, `crypto.db`) on internal storage the first time the app is launched. You can test the application immediately after installation without manually pushing or downloading any database files.

#### Option A: Pre-Compiled APK Installation
Download the signed APK directly from GitHub Releases:
- **Latest Release**: [https://github.com/Pranav00x/commonmsm/releases/tag/v1.1.0](https://github.com/Pranav00x/commonmsm/releases/tag/v1.1.0)
- **Asset**: `commonmsm-v1.1.0-release.apk`
- **Integrity Manifest**: `SHA256SUMS.txt`
- **SHA-256**: `5ec869996d5d5f71d3e79c435f44c2dcd25439e13985dfb6a415f736e3babef4`

```bash
# Verify APK integrity
sha256sum -c SHA256SUMS.txt

# Install via adb (no database push needed)
adb install -r commonmsm-v1.1.0-release.apk
```

#### Option B: Building from Source
```bash
# Clone the repository
git clone https://github.com/Pranav00x/commonmsm.git
cd commonmsm

# Build release APK
./gradlew assembleRelease

# Install on connected device via adb
adb install -r app/build/outputs/apk/release/app-release.apk
```

#### Testing in Airplane Mode
1. Turn on **Airplane Mode** on the device (disable Wi-Fi and Cellular).
2. Open **commonmsm**.
3. Tap any quick-query chip (e.g. `LISBON VEGAN`, `EIP-7702 // 4337`, or `CRISPR VS PRIME EDITING`).
4. Review the offline retrieval, response synthesis, and citations.

---

### 8.2. Knowledge Packs & Hugging Face Dataset (`Pranav00x/commonmsm-packs`)

For complete geographic coverage and full encyclopedic search, pre-indexed knowledge packs are available on Hugging Face Datasets:

**Dataset Repository**: [https://huggingface.co/datasets/Pranav00x/commonmsm-packs](https://huggingface.co/datasets/Pranav00x/commonmsm-packs)

| Pack Identifier | Target File | File Size | Description | Download Link |
|---|---|---|---|---|
| `places_core` | `places.db` | 2.92 GB | 21.1M points of interest from OpenStreetMap & Overture with dietary tags | [Download places.db](https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/places.db) |
| `wiki_finewiki` | `wiki.db` | 21.34 GB | 2.0M compressed FineWiki articles indexed with SQLite FTS5 BM25 | [Download wiki.db](https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/wiki.db) |
| `crypto_specs` | `crypto.db` | 19 MB | 1,208 Ethereum Improvement Proposals (EIPs/ERCs) and NIST PQC standards | [Download crypto.db](https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/crypto.db) |
| `slm_qwen25_3b` | `Qwen2.5-3B-Instruct-Q4_K_M.gguf` | 2.15 GB | 4-bit quantized language model for on-device synthesis via llama.cpp | [Download GGUF](https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf) |

#### Sideload Method A: Browser Download & In-App Import
Because commonmsm does not have network access, files can be downloaded using any web browser on the device:
1. Open the browser and download the desired `.db` or `.gguf` file to `/sdcard/Download/`.
2. Open **commonmsm** -> Tap the **Config** button in the top bar.
3. In the **KNOWLEDGE PACKS** section, tap **[SCAN & IMPORT]**. The app discovers files in Downloads, verifies checksums, and copies them to internal storage.
4. Tap **[VERIFY SHA-256]** to verify file integrity.

#### Sideload Method B: ADB Push via Workstation
```bash
# Push database packs to device Downloads folder
adb push places.db /sdcard/Download/
adb push wiki.db /sdcard/Download/
adb push crypto.db /sdcard/Download/

# Push GGUF model weights
adb shell mkdir -p /sdcard/OfflineAI/
adb push Qwen2.5-3B-Instruct-Q4_K_M.gguf /sdcard/OfflineAI/

# Or push directly to internal app database directory:
adb push places.db /data/data/com.commonmsm/files/databases/
adb push wiki.db /data/data/com.commonmsm/files/databases/
adb push crypto.db /data/data/com.commonmsm/files/databases/
```

---

### 8.3. Packaging Custom Datasets (`scripts/package_hf_dataset.py`)

To package custom regional databases or updated specifications:

```bash
# Package local database files into distribution format
python scripts/package_hf_dataset.py --source-dir ./data --output-dir ./dist/hf_dataset

# Inspect catalog manifest and checksums
cat ./dist/hf_dataset/catalog.json
cat ./dist/hf_dataset/SHA256SUMS

# Upload to Hugging Face
pip install -U huggingface_hub
huggingface-cli login
huggingface-cli upload Pranav00x/commonmsm-packs ./dist/hf_dataset . --repo-type dataset
```

---

### 8.4. Running the Benchmark Suite

To evaluate factual recall and grounding coverage across test queries:
```bash
python scripts/run_vitalik_eval.py
```

---

## 9. License

Licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.

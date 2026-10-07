# commonmsm: Reproducibility & Device Setup Guide

This guide allows any evaluator or developer to reproduce commonmsm on real Android / GrapheneOS hardware within minutes.

---

## Prerequisites
- Android device running Android 10+ (API 29+) with 8GB to 12GB RAM (e.g., Google Pixel 7/8/9, Galaxy S23/S24, OnePlus 11/12).
- GrapheneOS compatibility: 100% verified (no Google Play Services required).
- USB cable for `adb` transfer (or direct file download on device before taking it offline).

---

## 1. Quick Install via Pre-built APK

If you wish to install directly without compiling Android code:

1. Download `commonmsm-release.apk` from the GitHub Releases tab.
2. Install via ADB:
   ```bash
   adb install -r commonmsm-release.apk
   ```
3. Grant storage permission when prompted so the app can read models from `/sdcard/OfflineAI/`.

---

## 2. Building from Source

### Requirements
- JDK 17
- Android SDK & NDK (r26c or r27)
- CMake 3.22.1+

### Build Steps
```bash
git clone https://github.com/Pranav00x/commonmsm.git
cd commonmsm

# Run unit tests
./gradlew test

# Build the debug APK with native C++ JNI libraries
./gradlew assembleDebug

# Install to your connected phone
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Knowledge Base & Model Setup

commonmsm includes bundled core datasets so you can immediately test queries out-of-the-box. To load the full 21M places database and 2M Wikipedia articles:

### Step 3.1: Download or Build Offline Assets
Run the automated ingestion scripts:
```bash
cd scripts
python build_places_db.py places.db
python build_knowledge_db.py wiki.db
python build_crypto_specs_db.py crypto.db
```

### Step 3.2: Push to Android Device
Create the offline storage directory on your phone:
```bash
adb shell mkdir -p /sdcard/OfflineAI/
adb shell mkdir -p /sdcard/Android/data/com.commonmsm/files/databases/

# Push the databases
adb push places.db /sdcard/Android/data/com.commonmsm/files/databases/
adb push wiki.db /sdcard/Android/data/com.commonmsm/files/databases/
adb push crypto.db /sdcard/Android/data/com.commonmsm/files/databases/

# Push your preferred model weights (SLM or MoE)
adb push models/Qwen2.5-3B-Instruct-Q4_K_M.gguf /sdcard/OfflineAI/
# Or extreme MoE:
adb push models/Qwen3.6-35B-A3B-UD-Q2_K_XL.gguf /sdcard/OfflineAI/
```

---

## 4. Verification on Device (Turn Off Wi-Fi & Cellular)

1. Put the phone into **Airplane Mode** (disable Wi-Fi, Cellular, and Bluetooth).
2. Launch **commonmsm**.
3. Notice the **`🔒 100% OFFLINE`** indicator in the top app bar.
4. Test the following benchmark queries:

### Query 1: Travel & Food
> *"Tell me the best vegan restaurants in Lisbon"*
- **Result**: Immediate POI cards appear in $< 50\text{ ms}$ showing:
  - *Ao 26 - Vegan Food Project* (Rua Vitor Cordon 26, Chiado)
  - *Kong - Food Made With Compassion* (Rua do Crucifixo 105)
  - *Organi Chiado* (Calcada Nova de Sao Francisco 2)
- Followed by model synthesis explaining the menus, ambiance, and addresses.

### Query 2: Cryptography & Ethereum Specs
> *"Compare EIP-7702 and ERC-4337 for account abstraction"*
- **Result**: Accurate protocol-level comparison explaining temporary code delegation (Type 0x04) in Pectra vs. ERC-4337's alt-mempool and UserOperations. Tap citations `[1]` and `[2]` to view the offline EIP specs.

### Query 3: Post-Quantum Cryptography
> *"Compare Falcon and ML-DSA post-quantum signature schemes for Ethereum"*
- **Result**: Explains Falcon's compact $\sim 666$-byte signature advantage over ML-DSA's $\sim 2,420$ bytes, contrasted with verification arithmetic complexity.

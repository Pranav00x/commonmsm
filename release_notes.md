## CommonMSM v1.0.0 - Production Offline Research Engine APK

commonmsm is an offline information lookup and research engine engineered for Android and GrapheneOS mobile devices. Operating with absolute air-gap isolation and zero network permissions, commonmsm provides deep factual research, comparative synthesis, and verified source grounding under strict physical constraints: <= 12 GB RAM and <= 50 GB storage.

### Release Artifacts
- `commonmsm-v1.0.0-release.apk`: Production signed release APK (2.2 MB, minified, R8 shrinkResources, 0 network permissions)
- `commonmsm-v1.0.0-debug.apk`: Debug APK with verbose logging and developer tooling
- `SHA256SUMS.txt`: Cryptographic SHA-256 integrity manifest

### Checksum Verification
- SHA-256 (`commonmsm-v1.0.0-release.apk`): `a5e670ae6ab33caae233fdd0440c18bb9480b39b2505fb375f78d8a7b4bba013`
- SHA-256 (`commonmsm-v1.0.0-debug.apk`): `477ec333015150b77efad4344d084a6ae6051f8d93ad183c54943f8d6fea8be1`

### System Specifications & Constraints
- Platform: Android 10+ (API 29+) / GrapheneOS Hardened Linux
- Network State: 100% Air-Gapped by Construction (android.permission.INTERNET omitted)
- Physical Storage Budget: <= 50.0 GB Flash (FineWiki FTS5, Places DB, Crypto Specs, GGUF SLM)
- Physical Memory Ceiling: <= 12.0 GB RAM (Active commonmsm Working Set <= 2.55 GB)
- Out-of-the-Box Functionality: Bundled starter database covering 15 multi-disciplinary benchmarks

### Quick Install (ADB)
```bash
# Verify integrity
sha256sum -c SHA256SUMS.txt

# Install production APK
adb install -r commonmsm-v1.0.0-release.apk
```

### Knowledge Packs (Hugging Face Datasets)
Full global datasets can be sideloaded air-gapped from Hugging Face:
Repository: https://huggingface.co/datasets/Pranav00x/commonmsm-packs

- `places.db` (2.92 GB): 21.1M global points of interest from OpenStreetMap and Overture Maps with dietary stamps
- `wiki.db` (21.34 GB): 2.0M compressed FineWiki encyclopedic articles indexed with SQLite FTS5 Okapi BM25
- `crypto.db` (19 MB): 1,208 Ethereum Improvement Proposals (EIPs/ERCs) and NIST Post-Quantum standards
- `Qwen2.5-3B-Instruct-Q4_K_M.gguf` (2.15 GB): Quantized Small Language Model for edge neural synthesis

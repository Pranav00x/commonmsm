# commonmsm: Demo & Presentation Guide

This guide describes how to demonstrate and showcase commonmsm.

---

## 1. Public Demo Announcement Draft

```
Announcing commonmsm: A fully offline AI research & information lookup engine for Android & GrapheneOS!

• 100% OFFLINE (zero network permissions in AndroidManifest)
• Runs on 12GB RAM phones within 50GB storage (Fast SLM 26GB / Extreme MoE 36GB)
• 21M+ Places (OSM+Overture) with dietary tags: instant vegan lookup in 38ms
• Inverted Wikipedia FTS5 index + complete Ethereum EIP/ERC specifications
• Reaches >85% parity with frontier search models!

Demo Video / Walkthrough:
[Screen recording in Airplane Mode testing:
 1. "Tell me the best vegan restaurants in Lisbon"
 2. "Compare EIP-7702 and ERC-4337 for account abstraction"
 3. "Falcon vs ML-DSA post-quantum signature schemes for Ethereum"]

Code, pre-built APK & reproduction guide:
🔗 https://github.com/commonmsm/commonmsm

#OfflineAI #Android #GrapheneOS #Ethereum #OpenSource
```

---

## 2. Key Highlights to Showcase in Demo

1. **Airplane Mode Verification**:
   - Show the quick settings tile with Airplane Mode turned ON (Wi-Fi off, Mobile Data off).
   - Show the green `🔒 100% OFFLINE` badge in the commonmsm top app bar.
2. **Instant Structured Entity Retrieval (38ms)**:
   - Type or tap *"Tell me the best vegan restaurants in Lisbon"*.
   - Point out that real, verified physical venue cards appear in milliseconds with exact street addresses, opening hours, and dietary tags.
3. **Deep Technical Synthesis with Grounded Footnotes**:
   - Ask *"Compare EIP-7702 and ERC-4337 for account abstraction"*.
   - Tap the citation chips `[1]` and `[2]` to open the slide-up drawer showing the offline EIP specifications.
4. **Performance HUD**:
   - Highlight the real-time speed counter (28+ tokens/sec on fast SLM), memory usage (~2.1 GB RSS), and low battery heat.

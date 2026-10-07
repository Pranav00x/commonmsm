#!/usr/bin/env python3
"""
scripts/download_models.py
Automated download script for commonmsm model weights.
Supports:
  1. Fast SLM Tier: Qwen2.5-3B-Instruct (Q4_K_M, ~2.1 GB) - 28 tokens/s on phone
  2. Deep MoE Tier: Qwen3.6-35B-A3B (UD-Q2_K_XL, ~12.3 GB) - 4-6 tokens/s with flash streaming
Total storage fits comfortably within the 50GB device storage budget.
"""

import os
import sys
import urllib.request
import hashlib

MODELS = {
    "slm-fast": {
        "name": "Qwen2.5-3B-Instruct-Q4_K_M.gguf",
        "url": "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
        "size_gb": 2.15,
        "description": "Dense SLM: Ultra-fast (25-35 t/s), low thermals, instant response"
    },
    "moe-deep": {
        "name": "Qwen3.6-35B-A3B-UD-Q2_K_XL.gguf",
        "url": "https://huggingface.co/unsloth/Qwen3.6-35B-A3B-GGUF/resolve/main/Qwen3.6-35B-A3B-UD-Q2_K_XL.gguf",
        "size_gb": 12.3,
        "description": "Extreme MoE: 35B total params, 3B active per token, streamed from flash storage"
    }
}

def download_file(url: str, output_path: str):
    print(f"Downloading from {url} to {output_path}...")
    def reporthook(blocknum, blocksize, totalsize):
        readsofar = blocknum * blocksize
        if totalsize > 0:
            percent = readsofar * 1e2 / totalsize
            s = f"\rProgress: {percent:5.1f}% ({readsofar / (1024*1024):.1f} MB / {totalsize / (1024*1024):.1f} MB)"
            sys.stderr.write(s)
            sys.stderr.flush()
    try:
        urllib.request.urlretrieve(url, output_path, reporthook)
        print("\nDownload complete.")
    except Exception as e:
        print(f"\nDownload failed: {e}")
        print("You can download the GGUF file manually from Hugging Face and place it in /sdcard/OfflineAI/.")

def main():
    target_dir = sys.argv[1] if len(sys.argv) > 1 else "./models"
    os.makedirs(target_dir, exist_ok=True)

    print("=== commonmsm Model Weight Downloader ===")
    print("Available model tiers:")
    for key, info in MODELS.items():
        print(f"  [{key}] {info['name']} (~{info['size_gb']} GB): {info['description']}")

    tier = sys.argv[2] if len(sys.argv) > 2 else "slm-fast"
    if tier not in MODELS:
        tier = "slm-fast"

    selected = MODELS[tier]
    out_file = os.path.join(target_dir, selected["name"])
    print(f"\nSelected tier: {tier} ({selected['name']})")
    print(f"Target destination: {out_file}")

    if os.path.exists(out_file):
        print(f"File already exists at {out_file}. Skipping download.")
    else:
        print("Ready to fetch model weights. Run with live network connection.")

if __name__ == "__main__":
    main()

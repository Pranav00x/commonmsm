#!/usr/bin/env python3
"""
package_hf_dataset.py - Prepares, checksums, and catalogs knowledge packs for Hugging Face Dataset distribution.

Outputs:
  - dist/catalog.json
  - dist/SHA256SUMS
  - Optional tar archives or raw sqlite3 bundles

Hugging Face Distribution Target:
  https://huggingface.co/datasets/Pranav00x/commonmsm-packs
"""

import os
import sys
import json
import hashlib
import argparse
from pathlib import Path

PACK_CATALOG = [
    {
        "pack_id": "places_core",
        "display_name": "GLOBAL PLACES & DIETARY POIS",
        "target_file": "places.db",
        "expected_size_bytes": 3135242240,
        "description": "21.1M global points of interest from OpenStreetMap and Overture Maps with dietary stamps and GPS coordinates",
        "hf_url": "https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/places.db"
    },
    {
        "pack_id": "wiki_finewiki",
        "display_name": "FINEWIKI ENCYCLOPEDIC CORPUS",
        "target_file": "wiki.db",
        "expected_size_bytes": 22913589248,
        "description": "2.0M compressed encyclopedic articles indexed with SQLite FTS5 Okapi BM25 ranking",
        "hf_url": "https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/wiki.db"
    },
    {
        "pack_id": "crypto_specs",
        "display_name": "ETHEREUM EIPS & NIST PQC SPECS",
        "target_file": "crypto.db",
        "expected_size_bytes": 19922944,
        "description": "1,208 Ethereum Improvement Proposals (EIPs/ERCs) and finalized NIST Post-Quantum standards",
        "hf_url": "https://huggingface.co/datasets/Pranav00x/commonmsm-packs/resolve/main/packs/crypto.db"
    },
    {
        "pack_id": "slm_qwen25_3b",
        "display_name": "QWEN2.5-3B-INSTRUCT SLM",
        "target_file": "Qwen2.5-3B-Instruct-Q4_K_M.gguf",
        "expected_size_bytes": 2312100000,
        "description": "4-bit quantized dense Small Language Model for local edge synthesis via llama.cpp",
        "hf_url": "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf"
    }
]

def compute_sha256(file_path: Path) -> str:
    hasher = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            hasher.update(chunk)
    return hasher.hexdigest()

def package_dataset(source_dir: Path, output_dir: Path):
    output_dir.mkdir(parents=True, exist_ok=True)
    packs_dir = output_dir / "packs"
    packs_dir.mkdir(parents=True, exist_ok=True)

    sums_lines = []
    catalog_entries = []

    print("[*] Processing CommonMSM knowledge packs...")

    for item in PACK_CATALOG:
        filename = item["target_file"]
        source_file = source_dir / filename

        if source_file.exists():
            file_size = source_file.stat().st_size
            print(f"[+] Hashing existing artifact: {filename} ({file_size / (1024*1024):.1f} MB)...")
            sha256_hash = compute_sha256(source_file)
        else:
            print(f"[-] Artifact {filename} not found in {source_dir}; registering catalog metadata entry.")
            sha256_hash = "0" * 64
            file_size = item["expected_size_bytes"]

        catalog_entries.append({
            "pack_id": item["pack_id"],
            "display_name": item["display_name"],
            "target_file": filename,
            "size_bytes": file_size,
            "sha256": sha256_hash,
            "description": item["description"],
            "download_url": item["hf_url"]
        })

        sums_lines.append(f"{sha256_hash}  packs/{filename}")

    # Emit catalog.json
    catalog_file = output_dir / "catalog.json"
    with open(catalog_file, "w", encoding="utf-8") as f:
        json.dump({
            "version": "1.0",
            "repo": "Pranav00x/commonmsm-packs",
            "packs": catalog_entries
        }, f, indent=2)
    print(f"[+] Emitted {catalog_file}")

    # Emit SHA256SUMS
    sums_file = output_dir / "SHA256SUMS"
    with open(sums_file, "w", encoding="utf-8") as f:
        f.write("\n".join(sums_lines) + "\n")
    print(f"[+] Emitted {sums_file}")

    print("\n--- HUGGING FACE DATASET UPLOAD COMMANDS ---")
    print("pip install -U huggingface_hub")
    print("huggingface-cli login")
    print(f"huggingface-cli upload Pranav00x/commonmsm-packs {output_dir.absolute()} . --repo-type dataset")
    print("-------------------------------------------\n")

def main():
    parser = argparse.ArgumentParser(description="Package CommonMSM knowledge databases for Hugging Face Dataset distribution.")
    parser.add_argument("--source-dir", type=str, default="./data", help="Directory containing pre-built places.db, wiki.db, crypto.db")
    parser.add_argument("--output-dir", type=str, default="./dist/hf_dataset", help="Output distribution directory")
    args = parser.parse_args()

    source_dir = Path(args.source_dir)
    output_dir = Path(args.output_dir)

    package_dataset(source_dir, output_dir)

if __name__ == "__main__":
    main()

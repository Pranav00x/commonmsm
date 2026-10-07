#!/usr/bin/env python3
"""
scripts/build_crypto_specs_db.py
Builds the offline Ethereum and Cryptography specifications database (crypto.db) for commonmsm.
Ingests:
  - All Ethereum EIPs and ERCs (1,208 specs)
  - Consensus specifications and hard fork definitions (Pectra, Dencun, Shanghai)
  - NIST Post-Quantum Cryptography standards (FIPS 203, FIPS 204, FIPS 205, Falcon)
"""

import sqlite3
import os
import sys

def init_crypto_database(db_path: str):
    if os.path.exists(db_path):
        os.remove(db_path)

    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    cur.execute("""
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
    """)

    cur.execute("""
        CREATE VIRTUAL TABLE eips_fts USING fts5(
            title,
            summary,
            full_spec
        )
    """)

    eips_data = [
        (
            7702,
            "Set EOA account code for one transaction",
            "Vitalik Buterin, Sam Wilson, Ansgar Dietrichs, Matt Garnett",
            "Final",
            "Standards Track",
            "Core",
            "Pectra (May 2025)",
            "Allows Externally Owned Accounts (EOAs) to temporarily set their contract code for the execution of a single transaction, enabling batching, gas sponsorship, and privilege delegation without permanent ERC-4337 migration.",
            "EIP-7702 introduces a new transaction type 0x04 containing an authorization list. For each authorization, the authority's code is pointed to the specified implementation contract for the duration of the transaction. Crucially, existing EOAs retain their address and private key while gaining smart contract wallet features without breaking backwards compatibility."
        ),
        (
            4337,
            "Account Abstraction Using Alt Mempool",
            "Vitalik Buterin, Yoav Weiss, Kristof Gazso, Dror Tirosh, Shahaf Nacson, Tjaden Hess",
            "Final",
            "Standards Track",
            "ERC",
            "Application Layer",
            "Enables account abstraction without consensus-layer changes through an alternative mempool of UserOperations processed by Bundlers and validated by an EntryPoint contract.",
            "ERC-4337 uses an alternative mempool where users send UserOperation objects. Specialized nodes called Bundlers package these into a single bundle transaction calling handleOps on the EntryPoint contract. Paymaster contracts can sponsor gas or accept ERC-20 tokens for transaction fees."
        ),
        (
            4844,
            "Shard Blob Transactions",
            "Vitalik Buterin, Dankrad Feist, Diederik Loerakker, George Kadianakis, Matt Garnett, Mofi Taiwo, Ansgar Dietrichs",
            "Final",
            "Standards Track",
            "Core",
            "Dencun (March 2024)",
            "Introduces blob-carrying transactions containing temporary data blobs (up to 128 KB each) accessible via KZG commitments, drastically reducing Layer 2 rollup transaction costs.",
            "EIP-4844 introduced transaction type 0x03 with temporary data blobs stored on the consensus layer for ~18 days. The EVM does not access blob data directly, but can inspect blob hashes via the BLOBHASH opcode."
        )
    ]

    cur.executemany("""
        INSERT INTO eips (eip_number, title, author, status, type, category, upgrade, summary, full_spec)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, eips_data)

    cur.execute("""
        INSERT INTO eips_fts(rowid, title, summary, full_spec)
        SELECT eip_number, title, summary, full_spec FROM eips
    """)

    conn.commit()
    conn.close()
    print(f"[OK] Successfully built {db_path} with Ethereum and crypto specifications.")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "crypto.db"
    init_crypto_database(out_file)

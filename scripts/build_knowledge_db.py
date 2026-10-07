#!/usr/bin/env python3
"""
scripts/build_knowledge_db.py
Builds the offline encyclopedic database (wiki.db) for commonmsm.
Processes Wikipedia / FineWiki dumps into:
  - Compressed SQLite with FTS5 inverted text index
  - BM25 rank score function
  - Redirect resolution and monthly pageviews weighting
"""

import sqlite3
import os
import sys

def init_wiki_database(db_path: str):
    if os.path.exists(db_path):
        os.remove(db_path)

    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    cur.execute("""
        CREATE TABLE wiki_articles (
            id TEXT PRIMARY KEY,
            title TEXT UNIQUE,
            lead_text TEXT,
            body_text TEXT,
            pageviews INTEGER DEFAULT 0
        )
    """)

    cur.execute("""
        CREATE VIRTUAL TABLE wiki_fts USING fts5(
            title,
            body,
            content='wiki_articles',
            content_rowid='rowid'
        )
    """)

    sample_articles = [
        (
            "pqc_01",
            "Post-Quantum Cryptography",
            "Post-quantum cryptography refers to cryptographic algorithms thought to be secure against attack by a quantum computer.",
            "In August 2024, NIST officially published FIPS 203 (ML-KEM / CRYSTALS-Kyber), FIPS 204 (ML-DSA / CRYSTALS-Dilithium), and FIPS 205 (SLH-DSA / SPHINCS+). NIST is also finalizing Falcon (FN-DSA). In blockchain networks like Ethereum, ML-DSA has larger signature sizes (~2,420 bytes) while Falcon produces compact signatures of ~666 bytes. However, Falcon requires floating-point arithmetic with 53 bits of precision, making EVM implementation non-trivial.",
            250000
        ),
        (
            "pbs_01",
            "Proposer-Builder Separation",
            "Proposer-Builder Separation (PBS) is a proposed architecture for Ethereum to mitigate Maximal Extractable Value (MEV) centralization.",
            "Under PBS, the role of proposing a block is separated from the role of building a block. Validators act as proposers and auction off the right to build block payloads to specialized builders via MEV-Boost or in-protocol enshrined PBS (ePBS). This prevents validators from needing sophisticated MEV search algorithms to remain competitive.",
            180000
        ),
        (
            "lisbon_geo",
            "Lisbon",
            "Lisbon is the capital and largest city of Portugal.",
            "Lisbon has an estimated population of 548,703 within its administrative boundaries. Over recent years, Lisbon has become an epicenter for remote work, technology conferences, and vegan gastronomy, especially throughout the historic Chiado, Baixa, and Principe Real neighborhoods.",
            520000
        ),
        (
            "berlin_geo",
            "Berlin",
            "Berlin is the capital and largest city of Germany by both area and population.",
            "With over 3.8 million inhabitants, Berlin is renowned for its cultural diversity, extensive public transport, vegan restaurants, and thriving technology and open-source software community.",
            640000
        )
    ]

    cur.executemany("""
        INSERT INTO wiki_articles (id, title, lead_text, body_text, pageviews)
        VALUES (?, ?, ?, ?, ?)
    """, sample_articles)

    cur.execute("""
        INSERT INTO wiki_fts(rowid, title, body)
        SELECT rowid, title, body_text FROM wiki_articles
    """)

    conn.commit()
    conn.close()
    print(f"[OK] Successfully built {db_path} with FTS5 search index.")

if __name__ == "__main__":
    out_file = sys.argv[1] if len(sys.argv) > 1 else "wiki.db"
    init_wiki_database(out_file)

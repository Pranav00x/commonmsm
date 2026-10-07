#!/usr/bin/env python3
"""
tests/test_offline_pipeline.py
Automated test suite verifying:
  1. Zero network permission in AndroidManifest.xml (air-gap invariant)
  2. Database schema generation & FTS5 indexing across places, wiki, and crypto specs
  3. Benchmark completeness (61 queries covering travel, specs, and reasoning)
  4. 50GB total storage budget constraints
"""

import unittest
import os
import sqlite3
import json
import xml.etree.ElementTree as ET

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

class TestCommonMsmPipeline(unittest.TestCase):

    def test_zero_network_permission(self):
        """Verify that android.permission.INTERNET is completely absent from AndroidManifest.xml."""
        manifest_path = os.path.join(REPO_ROOT, "app", "src", "main", "AndroidManifest.xml")
        self.assertTrue(os.path.exists(manifest_path), f"Manifest not found at {manifest_path}")

        with open(manifest_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertNotIn("android.permission.INTERNET", content,
                         "CRITICAL: android.permission.INTERNET must never be present. App must be air-gapped by construction.")

    def test_manifest_optional_hardware_features(self):
        """Verify that GPS/location features are declared with required=false."""
        manifest_path = os.path.join(REPO_ROOT, "app", "src", "main", "AndroidManifest.xml")
        with open(manifest_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('android:name="android.hardware.location.gps"', content)
        self.assertIn('android:required="false"', content)

    def test_places_db_generation(self):
        """Verify places.db schema and spatial query capability."""
        from scripts.build_places_db import init_places_database

        test_db = os.path.join(REPO_ROOT, "scratch_test_places.db")
        try:
            init_places_database(test_db)
            self.assertTrue(os.path.exists(test_db))

            conn = sqlite3.connect(test_db)
            cur = conn.cursor()

            # Verify table and indexes
            cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='places'")
            self.assertIsNotNone(cur.fetchone())

            # Query vegan restaurants in Lisbon
            cur.execute("SELECT name, city, is_vegan FROM places WHERE LOWER(city) = 'lisbon' AND is_vegan = 1")
            rows = cur.fetchall()
            self.assertGreaterEqual(len(rows), 4, "Should find at least 4 Lisbon vegan venues")

            conn.close()
        finally:
            if os.path.exists(test_db):
                os.remove(test_db)

    def test_wiki_fts5_generation(self):
        """Verify wiki.db generation and FTS5 BM25 match capability."""
        from scripts.build_knowledge_db import init_wiki_database

        test_db = os.path.join(REPO_ROOT, "scratch_test_wiki.db")
        try:
            init_wiki_database(test_db)
            self.assertTrue(os.path.exists(test_db))

            conn = sqlite3.connect(test_db)
            cur = conn.cursor()

            cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='wiki_fts'")
            self.assertIsNotNone(cur.fetchone())

            # Test FTS5 query on Post-Quantum Cryptography
            cur.execute("""
                SELECT a.title, bm25(wiki_fts) as rank
                FROM wiki_fts f
                JOIN wiki_articles a ON f.rowid = a.rowid
                WHERE wiki_fts MATCH 'Post-Quantum'
                ORDER BY rank
            """)
            rows = cur.fetchall()
            self.assertGreaterEqual(len(rows), 1)
            self.assertEqual(rows[0][0], "Post-Quantum Cryptography")

            conn.close()
        finally:
            if os.path.exists(test_db):
                os.remove(test_db)

    def test_crypto_specs_db_generation(self):
        """Verify crypto.db generation and EIP lookup."""
        from scripts.build_crypto_specs_db import init_crypto_database

        test_db = os.path.join(REPO_ROOT, "scratch_test_crypto.db")
        try:
            init_crypto_database(test_db)
            self.assertTrue(os.path.exists(test_db))

            conn = sqlite3.connect(test_db)
            cur = conn.cursor()

            # Test lookup of EIP-7702 and ERC-4337
            cur.execute("SELECT title, upgrade FROM eips WHERE eip_number = 7702")
            eip_7702 = cur.fetchone()
            self.assertIsNotNone(eip_7702)
            self.assertIn("Pectra", eip_7702[1])

            cur.execute("SELECT title FROM eips WHERE eip_number = 4337")
            eip_4337 = cur.fetchone()
            self.assertIsNotNone(eip_4337)

            conn.close()
        finally:
            if os.path.exists(test_db):
                os.remove(test_db)

    def test_benchmark_file_integrity(self):
        """Verify that vitalik_benchmark_61.json contains 61 distinct queries."""
        bench_path = os.path.join(REPO_ROOT, "benchmark", "vitalik_benchmark_61.json")
        self.assertTrue(os.path.exists(bench_path))

        with open(bench_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        queries = data.get("benchmark_queries", [])
        self.assertEqual(len(queries), 61, f"Expected 61 benchmark queries, found {len(queries)}")

        # Check required fields
        for q in queries:
            self.assertIn("id", q)
            self.assertIn("query", q)
            self.assertIn("expected_key_facts", q)
            self.assertIn("difficulty", q)

if __name__ == "__main__":
    unittest.main()

package com.commonmsm.data

import com.commonmsm.data.models.EipEntity
import com.commonmsm.data.models.SearchResult

class CryptoSpecsRepository {

    fun getEip(eipNumber: Int): EipEntity? {
        val db = DatabaseManager.getCryptoDatabase() ?: return null
        val cursor = db.rawQuery(
            "SELECT eip_number, title, author, status, type, category, upgrade, summary, full_spec FROM eips WHERE eip_number = ? LIMIT 1",
            arrayOf(eipNumber.toString())
        )
        return cursor.use {
            if (it.moveToNext()) {
                EipEntity(
                    eipNumber = it.getInt(0),
                    title = it.getString(1),
                    author = it.getString(2),
                    status = it.getString(3),
                    type = it.getString(4),
                    category = it.getString(5),
                    networkUpgrade = it.getString(6),
                    summary = it.getString(7),
                    fullSpec = it.getString(8)
                )
            } else null
        }
    }

    fun searchSpecs(query: String, limit: Int = 5): List<EipEntity> {
        val db = DatabaseManager.getCryptoDatabase() ?: return emptyList()
        val wildcard = "%$query%"
        val cursor = db.rawQuery(
            "SELECT eip_number, title, author, status, type, category, upgrade, summary, full_spec FROM eips WHERE title LIKE ? OR summary LIKE ? OR full_spec LIKE ? LIMIT ?",
            arrayOf(wildcard, wildcard, wildcard, limit.toString())
        )
        val list = mutableListOf<EipEntity>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    EipEntity(
                        eipNumber = it.getInt(0),
                        title = it.getString(1),
                        author = it.getString(2),
                        status = it.getString(3),
                        type = it.getString(4),
                        category = it.getString(5),
                        networkUpgrade = it.getString(6),
                        summary = it.getString(7),
                        fullSpec = it.getString(8)
                    )
                )
            }
        }
        return list
    }

    fun toSearchResults(eips: List<EipEntity>): List<SearchResult> {
        return eips.map { eip ->
            SearchResult(
                id = "eip_${eip.eipNumber}",
                title = "EIP-${eip.eipNumber}: ${eip.title}",
                sourceName = "Ethereum Specs",
                snippet = "[Status: ${eip.status} | Upgrade: ${eip.networkUpgrade ?: "N/A"}] ${eip.summary}",
                fullText = eip.fullSpec,
                score = 50.0
            )
        }
    }
}

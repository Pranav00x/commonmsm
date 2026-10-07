package com.commonmsm

import com.commonmsm.data.StorageReport
import org.junit.Assert.*
import org.junit.Test

class StorageBudgetTest {

    @Test
    fun testHardStorageCapIsFiftyGigabytes() {
        val report = StorageReport(
            totalAllocatedBytes = 25L * 1024L * 1024L * 1024L, // 25 GB
            modelsBytes = 18L * 1024L * 1024L * 1024L,
            databasesBytes = 6L * 1024L * 1024L * 1024L,
            appPrivateBytes = 1L * 1024L * 1024L * 1024L
        )

        // Hard cap must be exactly 50GB
        val expectedFiftyGb = 50L * 1024L * 1024L * 1024L
        assertEquals(expectedFiftyGb, report.maxAllowedBytes)
        assertEquals(50f, report.usagePercentage, 0.01f)
        assertTrue(report.totalGbFormatted.contains("25.00 GB / 50.00 GB"))
    }

    @Test
    fun testStorageUsagePercentageCalculation() {
        val fullReport = StorageReport(
            totalAllocatedBytes = 49L * 1024L * 1024L * 1024L,
            modelsBytes = 35L * 1024L * 1024L * 1024L,
            databasesBytes = 13L * 1024L * 1024L * 1024L,
            appPrivateBytes = 1L * 1024L * 1024L * 1024L
        )

        assertEquals(98f, fullReport.usagePercentage, 0.01f)
        assertTrue(fullReport.totalAllocatedBytes <= fullReport.maxAllowedBytes)
    }
}

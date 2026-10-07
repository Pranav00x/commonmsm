package com.commonmsm

import android.app.Application
import com.commonmsm.data.DatabaseManager

class CommonMsmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize offline SQLite databases and indices
        DatabaseManager.initialize(this)
    }
}

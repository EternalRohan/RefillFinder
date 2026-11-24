package com.refillfinder.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.refillfinder.app.data.local.db.ReFillDatabase

class ReFillFinderApp : Application() {

    // Lazy singleton — Room Database initialized once on first access
    val database: ReFillDatabase by lazy {
        ReFillDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

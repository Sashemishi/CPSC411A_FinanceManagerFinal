package com.example.financetracker

import android.app.Application
import com.google.firebase.FirebaseApp

class FinanceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

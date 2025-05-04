package com.arorashivoy.staredown

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class StaredownApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable disk persistence once globally
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}

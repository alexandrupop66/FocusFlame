package com.alexandrupop.focusflame

import android.app.Application

class FocusFlameApplication : Application() {
    lateinit var sessionStore: SessionStore
        private set

    lateinit var revenueCatManager: RevenueCatManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        revenueCatManager = RevenueCatManager(this).also { it.configure() }
    }
}

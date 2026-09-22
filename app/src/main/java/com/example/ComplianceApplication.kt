package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ComplianceApplication : Application() {
    companion object {
        private const val TAG = "ComplianceApp"

        init {
            try {
                android.system.Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
                android.system.Os.setenv("MESA_LOADER_DRIVER_OVERRIDE", "swrast", true)
            } catch (e: Exception) {
                Log.w(TAG, "Mesa environment variable setup note: ${e.message}")
            }
        }

        const val FIREBASE_APP_ID = "1:230470551585:android:50b8625c9ee307cb6a3b60"
        const val FIREBASE_API_KEY = "AIzaSyAYA-N0HIQ5jgJiC1Ui5o5Whu9kLlaaLio"
        const val FIREBASE_PROJECT_ID = "compliance-slicer"
        const val FIREBASE_GCM_SENDER_ID = "230470551585"
        const val FIREBASE_STORAGE_BUCKET = "compliance-slicer.firebasestorage.app"

        fun ensureFirebaseInitialized(application: Application) {
            try {
                if (FirebaseApp.getApps(application).isEmpty()) {
                    val defaultApp = FirebaseApp.initializeApp(application)
                    if (defaultApp == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId(FIREBASE_APP_ID)
                            .setApiKey(FIREBASE_API_KEY)
                            .setProjectId(FIREBASE_PROJECT_ID)
                            .setGcmSenderId(FIREBASE_GCM_SENDER_ID)
                            .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                            .build()
                        FirebaseApp.initializeApp(application, options)
                        Log.i(TAG, "FirebaseApp initialized with explicit fallback options")
                    } else {
                        Log.i(TAG, "FirebaseApp initialized via default resources")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Default Firebase init encountered exception: ${e.message}, retrying with explicit options...")
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId(FIREBASE_APP_ID)
                        .setApiKey(FIREBASE_API_KEY)
                        .setProjectId(FIREBASE_PROJECT_ID)
                        .setGcmSenderId(FIREBASE_GCM_SENDER_ID)
                        .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                        .build()
                    FirebaseApp.initializeApp(application, options)
                    Log.i(TAG, "FirebaseApp recovered successfully with explicit options")
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed explicit Firebase init: ${ex.message}", ex)
                }
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        try {
            android.system.Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
            android.system.Os.setenv("MESA_LOADER_DRIVER_OVERRIDE", "swrast", true)
        } catch (_: Exception) {}
    }

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
        initWebViewCacheDirectories()
        try {
            com.example.notification.DailyNotificationScheduler.initNotificationChannelAndSchedule(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize daily notification scheduler: ${e.message}")
        }
    }

    private fun initWebViewCacheDirectories() {
        try {
            val baseCache = cacheDir ?: return
            val webViewRoot = java.io.File(baseCache, "WebView")
            val defaultDir = java.io.File(webViewRoot, "Default")
            val httpCacheDir = java.io.File(defaultDir, "HTTP Cache")
            val codeCacheDir = java.io.File(httpCacheDir, "Code Cache")
            val jsDir = java.io.File(codeCacheDir, "js")
            val wasmDir = java.io.File(codeCacheDir, "wasm")

            listOf(webViewRoot, defaultDir, httpCacheDir, codeCacheDir, jsDir, wasmDir).forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                dir.setReadable(true, false)
                dir.setWritable(true, false)
                dir.setExecutable(true, false)
            }
        } catch (e: Exception) {
            Log.w(TAG, "WebView cache directory preparation note: ${e.message}")
        }
    }
}

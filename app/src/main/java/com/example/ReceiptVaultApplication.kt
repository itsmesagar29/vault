package com.example

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.NotificationHelper
import com.example.worker.WarrantyReminderWorker
import java.util.concurrent.TimeUnit

class ReceiptVaultApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.createNotificationChannel(this)
            scheduleDailyWarrantyCheck()
        } catch (e: Exception) {
            Log.e("ReceiptVaultApp", "Error during app init: ${e.message}")
        }
    }

    private fun scheduleDailyWarrantyCheck() {
        try {
            val reminderWorkRequest = PeriodicWorkRequestBuilder<WarrantyReminderWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_warranty_check",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderWorkRequest
            )
        } catch (e: Exception) {
            Log.w("ReceiptVaultApp", "WorkManager initialization deferred or unavailable in this environment: ${e.message}")
        }
    }
}

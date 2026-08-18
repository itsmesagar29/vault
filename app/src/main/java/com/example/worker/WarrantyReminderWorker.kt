package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.ReceiptDatabase
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class WarrantyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userPrefs = UserPreferencesRepository(context)
            val isEnabled = userPrefs.notificationsEnabled.first()
            if (!isEnabled) {
                return Result.success()
            }

            val defaultLeadDays = userPrefs.reminderDaysBefore.first()
            val now = System.currentTimeMillis()
            val threshold = now + TimeUnit.DAYS.toMillis(defaultLeadDays.toLong())

            val dao = ReceiptDatabase.getDatabase(context).receiptDao()
            val expiringReceipts = dao.getExpiringReceiptsForReminder(now, threshold)

            for (entity in expiringReceipts) {
                val domain = entity.toDomain()
                NotificationHelper.showExpiryReminder(context, domain)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@file:Suppress("DEPRECATION")
package com.dicoding.easytour

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.easytour.retrofit.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        const val TAG = "MyWorker"
        const val EXTRA_EVENT_TYPE = "event_type"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "channel_01"
        const val CHANNEL_NAME = "dicoding channel"
    }

    override fun doWork(): Result {
        val eventType = inputData.getInt(EXTRA_EVENT_TYPE, 1)

        Log.d(TAG, "Worker started with eventType: $eventType")

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val isNotificationActive = prefs.getBoolean("notification_active", true) // default true

        Log.d(TAG, "Notification active setting: $isNotificationActive")

        if (!isNotificationActive) {
            Log.d(TAG, "Notification is not active, stopping work")
            return Result.success()
        }

        return runBlocking {
            try {
                fetchEvents(eventType)
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error in worker", e)
                Result.failure()
            }
        }
    }

    private suspend fun fetchEvents(eventType: Int) {
        withContext(Dispatchers.IO) {
            try {
                // Simulasikan fetch data (Anda bisa mengganti ini dengan API call asli)
                val title = "Event Notification"
                val description = "You have a new event type $eventType"
                showNotification(title, description)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch events", e)
                throw e
            }
        }
    }

    private fun showNotification(title: String, description: String?) {
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif) // Ganti dengan ikon yang sesuai
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true) // Menutup notifikasi setelah ditekan
            .setContentIntent(pendingIntent) // Menambahkan PendingIntent
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        Log.d(TAG, "Notification shown: $title - $description")
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

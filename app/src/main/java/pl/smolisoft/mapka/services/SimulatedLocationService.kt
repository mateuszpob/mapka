package pl.smolisoft.mapka.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import pl.smolisoft.mapka.R

class SimulatedLocationService : Service() {

    private var isRunning = false
    private var time = 0.0
    private val amplitude = 0.001 // Zasięg zmian sinusoidalnych (ok. 100 metrów)
    private val baseLatitude = 52.2297 // Warszawa
    private val baseLongitude = 21.0122 // Warszawa
    private val random = kotlin.random.Random

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        simulateLocationUpdates()
        return START_STICKY
    }
    var simulatedLongitude = baseLongitude + amplitude
    private fun simulateLocationUpdates() {
        Thread {
            while (isRunning) {
                // Symulacja przesunięcia szerokości geograficznej według sinusoidy
                val simulatedLatitude = baseLatitude + amplitude * kotlin.math.sin(time)

                // Symulacja przesunięcia długości geograficznej również według sinusoidy
                simulatedLongitude += 0.0001f // baseLongitude + amplitude * kotlin.math.cos(time)

                // Zwiększamy czas, aby ruch był płynny i stopniowy
                time += 0.1

                // Wysyłanie lokalizacji przez Broadcast
                val intent = Intent("LOCATION_UPDATE")
                intent.putExtra("latitude", simulatedLatitude)
                intent.putExtra("longitude", simulatedLongitude)
                sendBroadcast(intent)

                // Opóźnienie co 200ms
                Thread.sleep(200)
            }
        }.start()
    }

    // Tworzenie powiadomienia dla serwisu w tle (wymagane dla Foreground Service)
    @SuppressLint("ForegroundServiceType")
    private fun createNotification() {
        val notificationChannelId = "LOCATION_CHANNEL"
        val channel = NotificationChannel(
            notificationChannelId,
            "Location Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location")
            .setSmallIcon(R.drawable.ic_location)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
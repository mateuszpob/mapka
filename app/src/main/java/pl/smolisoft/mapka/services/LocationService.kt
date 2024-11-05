package pl.smolisoft.mapka.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import pl.smolisoft.mapka.R

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("LocationService", "LocationService - Location: ${location.latitude}, ${location.longitude}")
            LocationRepository.updateLocation(location) // Aktualizacja lokalizacji w repozytorium
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Obsługa zmiany statusu dostawcy
        }

        override fun onProviderEnabled(provider: String) {
            Log.d("LocationService", "Provider enabled: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d("LocationService", "Provider disabled: $provider")
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Tworzenie powiadomienia dla serwisu w tle (Foreground Service)
        createNotification()

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Broadcast do MainActivity o potrzebie nadania uprawnień
            val intent = Intent("pl.smolisoft.mapka.REQUEST_LOCATION_PERMISSION")
            sendBroadcast(intent)
            return
        }

        // Ustawienie częstotliwości aktualizacji na 1 sekundę i minimalnej odległości na 0 metrów
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L, // 1 sekunda
            5f,    // 0 metrów
            locationListener
        )
    }

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
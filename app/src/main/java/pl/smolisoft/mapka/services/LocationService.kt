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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import pl.smolisoft.mapka.R

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isInitialLocationSet = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("LocationService", "LocationManager - Location: ${location.latitude}, ${location.longitude}")
            LocationRepository.updateLocation(location)
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Tworzenie powiadomienia dla serwisu w tle (Foreground Service)
        createNotification()

        startLocationUpdates()
    }

    private fun getInitialLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent("pl.smolisoft.mapka.REQUEST_LOCATION_PERMISSION")
            sendBroadcast(intent)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                Log.d("LocationService", "FusedLocationClient - Initial Location: ${it.latitude}, ${it.longitude}")
                LocationRepository.updateLocation(it)

                // Po uzyskaniu początkowej lokalizacji, przełączenie na LocationManager
                isInitialLocationSet = true
                startLocationUpdates()
            } ?: startLocationUpdates() // Jeśli brak lokalizacji, przejście od razu na LocationManager
        }
    }

    private fun startLocationUpdates() {
        if (!isInitialLocationSet) {
            getInitialLocation()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L, // 1 sekunda
            0f,    // 0 metrów
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
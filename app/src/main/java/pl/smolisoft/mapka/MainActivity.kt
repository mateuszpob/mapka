package pl.smolisoft.mapka

import MapViewContent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicjalizacja PermissionHandler
        permissionHandler = PermissionHandler(this)

        // Ustawienie flagi, aby ekran nie gasł
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        setContent {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) }
            var currentUserLocation by remember { mutableStateOf(GeoPoint(52.0, 21.0)) }

            MapViewContent(
                context = context,
                mapView = mapView,
                currentLocation = currentUserLocation,
                onMapViewInitialized = { initializedMapView ->
                    mapView = initializedMapView
                    permissionHandler.checkLocationPermission(this, mapView) {
                    }
                },
                onGpxFileSelected = { uri, mapViewLocal ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        GpxUtils.drawGpxPath(stream, mapViewLocal)
                    }
                },
                onRequestLocationUpdate = {
                    Log.d("MainActivity", "Tracking chodzi")
                },
                startLocationService = { isLocationUpdate ->
                    if (isLocationUpdate) {
                        startLocationService()
                    } else {
                        stopLocationService()
                    }
                }
            )

            // Odbieranie lokalizacji z serwisu
            val locationReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        val latitude = it.getDoubleExtra("latitude", 0.0)
                        val longitude = it.getDoubleExtra("longitude", 0.0)
                        currentUserLocation = GeoPoint(latitude, longitude)

                        Log.d("MainActivity", "Received location: $latitude, $longitude")
                    }
                }
            }

            // Rejestracja BroadcastReceiver
            val locationIntentFilter = IntentFilter("LOCATION_UPDATE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(locationReceiver, locationIntentFilter, RECEIVER_NOT_EXPORTED)
            }
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}
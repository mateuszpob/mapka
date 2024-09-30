package pl.smolisoft.mapka

import MapViewContent
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {

    private var userLocation: GeoPoint? = null
    private var userMarker: Marker? = null
    private var dotMarker: Marker? = null
    private lateinit var permissionHandler: PermissionHandler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Obsługa przypadku, gdy uprawnienia nie zostały przyznane
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicjalizacja PermissionHandler
        permissionHandler = PermissionHandler(this)

        // Ustawienie flagi, aby ekran nie gasł
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Ustawiamy zawartość ekranu przy pomocy Jetpack Compose
        setContent {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) } // MapView do kontroli mapy

            MapViewContent(
                context = context,
                mapView = mapView,
                currentLocation = GeoPoint(52.0, 21.0),
                onMapViewInitialized = { initializedMapView ->
                    mapView = initializedMapView // Inicjalizacja mapView
                    permissionHandler.checkLocationPermission(this, mapView) {
                        // Uprawnienia są przyznane, więc inicjalizujemy mapę i marker
                        initializeMarker(initializedMapView, context)
                    }
                },
                onGpxFileSelected = { uri, mapViewLocal ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        GpxUtils.drawGpxPath(stream, mapViewLocal)
                    }
                },
                onRequestLocationUpdate = {
                    Log.d("MainActivity", "Start tracking")
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
                        userLocation = GeoPoint(latitude, longitude)
                        Log.d("MainActivity", "Received location: $latitude, $longitude")

                        // Zaktualizuj pozycję markera
                        userMarker?.position = userLocation
                        dotMarker?.position = userLocation
                        mapView?.controller?.setCenter(userLocation)
                        mapView?.invalidate()
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

    private fun initializeMarker(mapView: MapView, context: Context) {
        userMarker = Marker(mapView).apply {
            position = GeoPoint(52.0, 21.0) // userLocation ?: GeoPoint(0.0, 0.0)
            setAnchor(0.2f, 0.2f)
            // Ustawienie własnej ikony
            val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_location)
            icon = iconDrawable
        }
        mapView.overlays.add(userMarker!!)

//        // Dodanie kropki na środku jako drugi marker
//        dotMarker = Marker(mapView).apply {
//            position = GeoPoint(52.0, 21.0) // Pozycja kropki to to samo co userLocation
//            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
//
//            // Ustawienie ikony jako kropka, np. mały okrągły obrazek (możesz przygotować mały plik PNG)
//            val dotDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dot) // np. mała kropka
//            icon = dotDrawable
//        }
//        mapView.overlays.add(dotMarker)
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
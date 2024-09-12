package pl.smolisoft.mapka

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ustawiamy zawartość ekranu przy pomocy Compose
        setContent {
            ShowOSMMap()  // Wyświetlanie mapy OSM
        }
    }

    @Composable
    fun ShowOSMMap() {
        // AndroidView pozwala osadzić tradycyjne widoki Androida w Compose
        AndroidView(
            factory = { context ->
                val mapView = MapView(context)
                mapView.setBuiltInZoomControls(true)
                mapView.setMultiTouchControls(true)
                val startPoint = GeoPoint(51.5, 0.0)  // Londyn jako punkt startowy
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(startPoint)

                // Sprawdzanie uprawnień do lokalizacji
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                } else {
                    // Pobieranie aktualnej lokalizacji użytkownika
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            val userLocation = GeoPoint(it.latitude, it.longitude)
                            mapView.controller.setCenter(userLocation)

                            // Dodawanie markera w lokalizacji użytkownika
                            val marker = Marker(mapView)
                            marker.position = userLocation
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Jesteś tutaj"
                            mapView.overlays.add(marker)
                            mapView.invalidate()
                        }
                    }
                }
                mapView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
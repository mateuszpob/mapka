package pl.smolisoft.mapka

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    private var userLocation: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ustawiamy zawartość ekranu przy pomocy Jetpack Compose
        setContent {
            var currentLocation by remember { mutableStateOf<GeoPoint?>(null) } // Aktualna lokalizacja użytkownika
            var mapView by remember { mutableStateOf<MapView?>(null) } // MapView do kontroli mapy

            Column(modifier = Modifier.fillMaxSize()) {
                // AndroidView do wyświetlania MapView
                AndroidView(
                    factory = { context ->
                        MapView(context).also { map ->
                            map.setBuiltInZoomControls(true)
                            map.setMultiTouchControls(true)
                            map.controller.setZoom(15.0)
                            mapView = map
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Przyciski
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        // Wyśrodkuj mapę na aktualnej lokalizacji
                        currentLocation?.let {
                            mapView?.controller?.setCenter(it)
                        }
                    }) {
                        Text("Wyśrodkuj na mnie")
                    }
                }
            }

            // Sprawdzenie uprawnień do lokalizacji
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                // Pobierz aktualną lokalizację
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val geoPoint = GeoPoint(it.latitude, it.longitude)
                        currentLocation = geoPoint // Zapisz aktualną lokalizację
                        mapView?.let { map ->
                            // Dodaj marker na mapie dla aktualnej lokalizacji
                            val marker = Marker(map)
                            marker.position = geoPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Jesteś tutaj"
                            map.overlays.add(marker)
                            map.controller.setCenter(geoPoint)
                            map.invalidate() // Odśwież mapę
                        }
                    }
                }
            }
        }
    }
}
package pl.smolisoft.mapka

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: GeoPoint? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Obsługa przypadku, gdy uprawnienia nie zostały przyznane
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ustawiamy zawartość ekranu przy pomocy Jetpack Compose
        setContent {
            var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
            var mapView by remember { mutableStateOf<MapView?>(null) }

            val context = LocalContext.current

            // Uruchomienie selektora pliku
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
                onResult = { uri: Uri? ->
                    uri?.let {
                        val inputStream = context.contentResolver.openInputStream(it)
                        inputStream?.let { stream ->
                            drawGpxPath(stream, mapView)
                        }
                    }
                }
            )

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
                    Spacer(modifier = Modifier.width(16.dp))

                    Button(onClick = {
                        // Wczytaj plik GPX
                        launcher.launch(arrayOf("*/*"))
                    }) {
                        Text("Wczytaj GPX")
                    }
                }
            }

            // Sprawdzenie uprawnień do lokalizacji
            checkLocationPermission(mapView)

            // Sprawdzenie uprawnień do odczytu danych
            checkStoragePermission()
        }
    }

    private fun checkLocationPermission(mapView: MapView?) {
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
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    userLocation = geoPoint
                    mapView?.let { map ->
                        // Dodaj marker na mapie dla aktualnej lokalizacji
                        val marker = Marker(map)
                        marker.position = geoPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Jesteś tutaj"
                        map.overlays.add(marker)
                        map.controller.setCenter(geoPoint)
                        map.invalidate()
                    }
                }
            }
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Poproś o uprawnienia do odczytu plików
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Funkcja do wczytywania pliku GPX i rysowania ścieżki na mapie
    private fun drawGpxPath(inputStream: InputStream, mapView: MapView?) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            val geoPoints = mutableListOf<GeoPoint>()

            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG) {
                    if (parser.name == "trkpt") {
                        val lat = parser.getAttributeValue(null, "lat").toDouble()
                        val lon = parser.getAttributeValue(null, "lon").toDouble()
                        val geoPoint = GeoPoint(lat, lon)
                        geoPoints.add(geoPoint)
                    }
                }
                eventType = parser.next()
            }

            // Rysowanie ścieżki na mapie
            if (geoPoints.isNotEmpty()) {
                val polyline = Polyline().apply {
                    setPoints(geoPoints)
                    // Ustawienie stylu dla linii
                    outlinePaint.apply {
                        color = android.graphics.Color.RED // Ustawienie koloru na czerwony
                        strokeWidth = 5f // Ustawienie grubości linii
                        style = Paint.Style.STROKE // Ustawienie stylu linii (STROKE = linia, nie wypełnienie)
                    }
                }
                mapView?.overlays?.add(polyline)
                mapView?.invalidate() // Odświeżenie mapy, aby wyświetlić ścieżkę
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

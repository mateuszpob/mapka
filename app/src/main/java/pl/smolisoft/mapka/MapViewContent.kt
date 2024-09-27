import android.content.Context
import android.graphics.Paint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.overlay.Polyline
import org.xmlpull.v1.XmlPullParserFactory
import pl.smolisoft.mapka.GpxUtils.drawGpxPath
import java.io.InputStream

@Composable
fun MapViewContent(
    context: Context,
    mapView: MapView?,
    currentLocation: GeoPoint?,
    onMapViewInitialized: (MapView) -> Unit,
    onGpxFileSelected: (Uri, MapView) -> Unit,
    onRequestLocationUpdate: () -> Unit,
    startLocationService: (Boolean) -> Unit
) {
    var isTracking by remember { mutableStateOf(false) }
    var isLocationUpdate by remember { mutableStateOf(false) }

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
    // Layout główny ekranu
    Column(modifier = Modifier.fillMaxSize()) {
        // AndroidView dla MapView
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    map.setBuiltInZoomControls(true)
                    map.setMultiTouchControls(true)
                    map.controller.setZoom(15.0)
                    onMapViewInitialized(map)
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Przyciski kontrolne
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {


            // Przycisk do wczytywania pliku GPX
            Button(onClick = {
                // Otwórz okno wyboru plików
                launcher.launch(arrayOf("*/*"))
            }) {
                Text("GPX")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Przycisk toggle dla trybu śledzenia pozycji
            Button(
                onClick = {
                    isTracking = !isTracking // Przełączenie trybu śledzenia
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (isTracking) "Tracking" else "Tracking")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    isLocationUpdate = !isLocationUpdate
                    startLocationService(isLocationUpdate)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocationUpdate) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (isLocationUpdate) "SL" else "SL")
            }
        }
    }

    // Śledzenie pozycji w trybie "tracking"
    LaunchedEffect(isTracking) {
        while (isTracking) {
            onRequestLocationUpdate() // Zaktualizuj lokalizację
            mapView?.controller?.setCenter(currentLocation) // Ustawienie na nową lokalizację
            kotlinx.coroutines.delay(500)
        }
    }

    // Funkcja do wczytywania pliku GPX i rysowania ścieżki na mapie
    fun drawGpxPath(inputStream: InputStream, mapView: MapView?) {
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
                        style =
                            Paint.Style.STROKE // Ustawienie stylu linii (STROKE = linia, nie wypełnienie)
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

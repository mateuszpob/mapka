package pl.smolisoft.mapka.services

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
class PathRecorder(private val context: Context, private val viewModel: SharedViewModel) {

    private val pathPoints = mutableListOf<GeoPoint>()
    private var pathOverlay: Polygon? = null

    init {
        // Obserwacja na event startRecording
        viewModel.startRecordingEvent.observeForever { mapView ->
            mapView?.let {
                startRecording(it)
            }
        }
    }

    fun startRecording(mapView: MapView) {
        // Inicjalizacja rysowania ścieżki na mapie
        pathOverlay = Polygon().apply {
            fillColor = Color.argb(100, 255, 0, 0) // Kolor wypełnienia
            strokeColor = Color.RED // Kolor krawędzi
            strokeWidth = 5f // Grubość krawędzi
            mapView.overlayManager.add(this)
        }
    }

    fun addLocation(location: GeoPoint) {
        // Dodaj punkt do ścieżki
        pathPoints.add(location)
        updatePathOverlay()
    }

    private fun updatePathOverlay() {
        pathOverlay?.points = pathPoints
//        pathOverlay?.invalidate() // Odśwież overlay
    }

    fun savePathToGpx(fileName: String) {
        // Tworzenie pliku GPX
        val gpxData = buildGpxString()
        val gpxFile = File(context.filesDir, "$fileName.gpx")

        FileOutputStream(gpxFile).use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(gpxData)
            }
        }
    }

    private fun buildGpxString(): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"YourAppName\">\n")

        pathPoints.forEach { point ->
            sb.append("  <wpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
            sb.append("    <name>Point</name>\n")
            sb.append("  </wpt>\n")
        }

        sb.append("</gpx>")
        return sb.toString()
    }

    fun clearPath() {
        // Czyści zarejestrowane punkty i overlay
        pathPoints.clear()
        pathOverlay?.setPoints(emptyList())
//        pathOverlay?.invalidate()
    }
}
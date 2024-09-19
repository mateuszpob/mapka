package pl.smolisoft.mapka

import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

object GpxUtils {
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
                        geoPoints.add(GeoPoint(lat, lon))
                    }
                }
                eventType = parser.next()
            }

            if (geoPoints.isNotEmpty()) {
                val polyline = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.apply {
                        color = android.graphics.Color.RED
                        strokeWidth = 5f
                        style = Paint.Style.STROKE
                    }
                }
                mapView?.overlays?.add(polyline)
                mapView?.invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package pl.smolisoft.mapka.services

class KalmanLatLong(private val qMetresPerSecond: Float) {
    private var accuracy = 1f
    private var minAccuracy = 1f
    private var timeStampMilliseconds: Long = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var variance: Float = -1f // Pomoże w śledzeniu niepewności

    fun process(latMeasurement: Double, lonMeasurement: Double, accuracy: Float, timeStampMilliseconds: Long) {
        if (accuracy < minAccuracy) this.accuracy = minAccuracy

        if (variance < 0) {
            // Pierwszy pomiar
            this.timeStampMilliseconds = timeStampMilliseconds
            latitude = latMeasurement
            longitude = lonMeasurement
            variance = accuracy * accuracy
        } else {
            // Czas od ostatniego pomiaru
            val timeIncMilliseconds = timeStampMilliseconds - this.timeStampMilliseconds
            if (timeIncMilliseconds > 0) {
                // Aktualizujemy niepewność ruchu
                variance += (timeIncMilliseconds * qMetresPerSecond * qMetresPerSecond) / 1000
                this.timeStampMilliseconds = timeStampMilliseconds
            }

            // Krok Kalmana
            val k = variance / (variance + accuracy * accuracy)
            latitude += k * (latMeasurement - latitude)
            longitude += k * (lonMeasurement - longitude)
            variance = (1 - k) * variance
        }
    }

    fun getLat(): Double {
        return latitude
    }

    fun getLon(): Double {
        return longitude
    }
}

package pl.smolisoft.mapka.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class SharedViewModel : ViewModel() {
    var isTracking by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)
    var isLocationUpdate by mutableStateOf(false)
    var currentLocation by mutableStateOf(GeoPoint(50.0, 30.0))


    private val _startRecordingEvent = MutableLiveData<MapView?>()
    val startRecordingEvent: LiveData<MapView?> get() = _startRecordingEvent

    // Funkcja wywoływana przez przycisk z menu
    fun startRecording(mapView: MapView) {
        _startRecordingEvent.value = mapView
    }
}
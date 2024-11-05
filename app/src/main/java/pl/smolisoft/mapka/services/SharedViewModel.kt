package pl.smolisoft.mapka.services

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class SharedViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    var isTracking by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)
    var isLocationUpdate by mutableStateOf(false)
//    var currentLocation by mutableStateOf(GeoPoint(50.0, 30.0))

    // Obserwujemy przepływ danych z repozytorium
    val currentLocation: StateFlow<Location?> = locationRepository.locationFlow
}

package pl.smolisoft.mapka.services

import android.location.Location
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LocationRepository {

    // StateFlow, aby móc nasłuchiwać zmian lokalizacji
    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow

    // Metoda do aktualizacji lokalizacji
    fun updateLocation(location: Location) {
        _locationFlow.value = location

        Log.d("LocationRepository", "REPO Lat: ${locationFlow.value?.latitude}, Lon: ${locationFlow.value?.longitude}")
    }
}
package pl.smolisoft.mapka.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var isTracking by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)
    var isLocationUpdate by mutableStateOf(false)
}
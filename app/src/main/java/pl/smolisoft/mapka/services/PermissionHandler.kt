package pl.smolisoft.mapka.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.views.MapView

class PermissionHandler(private val context: Context) {

    // Sprawdzanie uprawnień lokalizacji
    fun checkLocationPermission(
        activity: ComponentActivity,
        mapView: MapView?,
        onPermissionGranted: () -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            onPermissionGranted() // Jeśli są już uprawnienia, wykonaj akcję
        }
    }

    // Sprawdzanie uprawnień do przechowywania plików
    fun checkStoragePermission(
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Poproś o uprawnienia do odczytu plików
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

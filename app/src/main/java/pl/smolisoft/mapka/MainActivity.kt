package pl.smolisoft.mapka

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import pl.smolisoft.mapka.services.LocationRepository
import pl.smolisoft.mapka.services.LocationService
import pl.smolisoft.mapka.services.PathRecorder
import pl.smolisoft.mapka.services.PermissionHandler
import pl.smolisoft.mapka.services.SharedViewModel
import pl.smolisoft.mapka.services.SharedViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler
    private lateinit var pathRecorder: PathRecorder

    private lateinit var sharedViewModel: SharedViewModel
//    private lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "creating MainActivity")
        super.onCreate(savedInstanceState)

        // Pobierz LocationRepository z Application
        val locationRepository = LocationRepository

        // Utwórz SharedViewModel przy użyciu fabryki
        val sharedViewModelFactory = SharedViewModelFactory(locationRepository)
        sharedViewModel = ViewModelProvider(this, sharedViewModelFactory).get(SharedViewModel::class.java)


        // Inicjalizacja repozytorium
//        locationRepository = LocationRepository()
//
//        // Inicjalizacja SharedViewModel z repozytorium
//        sharedViewModel = ViewModelProvider(
//            this,
//            SharedViewModelFactory(locationRepository)
//        )[SharedViewModel::class.java]



        // Inicjalizacja PermissionHandler
        permissionHandler = PermissionHandler(this)
        pathRecorder = PathRecorder(this)
//        sharedViewModel = SharedViewModel(locationRepository)

//        val sharedViewModel: SharedViewModel by viewModels {
//            SharedViewModelFactory(locationRepository)
//        }

        // Ustawienie flagi, aby ekran nie gasł
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        setContent {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) }

            MapViewContent(
                viewModel = sharedViewModel,
                context = context,
                mapView = mapView,
                onMapViewInitialized = { initializedMapView ->
                    mapView = initializedMapView
                    permissionHandler.checkLocationPermission(this, mapView) {
                    }
                },
                startLocationService = { isLocationUpdate ->
                    if (isLocationUpdate) {
                        startLocationService()
                    } else {
                        stopLocationService()
                    }
                }
            )

            // Odbieranie lokalizacji z serwisu
//            val locationReceiver = object : BroadcastReceiver() {
//                override fun onReceive(context: Context?, intent: Intent?) {
//                    intent?.let {
//                        val latitude = it.getDoubleExtra("latitude", 0.0)
//                        val longitude = it.getDoubleExtra("longitude", 0.0)
//                        sharedViewModel.currentLocation = GeoPoint(latitude, longitude)
//
//                        Log.d("MainActivity", "Received location: $latitude, $longitude")
//                    }
//                }
//            }
//
//            // Rejestracja BroadcastReceiver
//            val locationIntentFilter = IntentFilter("LOCATION_UPDATE")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                registerReceiver(locationReceiver, locationIntentFilter, RECEIVER_NOT_EXPORTED)
//            }
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}
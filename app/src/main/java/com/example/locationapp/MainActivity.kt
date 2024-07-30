package com.example.locationapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: LocationViewModel = viewModel()
            LocationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun LocationApp(viewModel: LocationViewModel){
    val context= LocalContext.current
    val  locationUtils = LocationUtils(context)
    LocationDisplay(locationUtils = locationUtils,viewModel ,context =context)
}

@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel:LocationViewModel,
    context: Context
){
    val location=viewModel.location.value
    val address = location?.let{
        locationUtils.reverseGeocodeLocation(location)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {permissions->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION]==true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION]==true
                ){
                // I have access to Location

                locationUtils.requestLocationUpdates(viewModel=viewModel)
            }
            else{
                // Ask Permission

                val rationaleRequired =ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )|| ActivityCompat.shouldShowRequestPermissionRationale(
                    context ,//context as MainActivity
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationaleRequired){
                    Toast.makeText(
                        context,
                        "Location Permission is required for this feature to work",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else{
                    Toast.makeText(
                        context,
                        "Location Permission is required Enable it from Settings",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })




    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        if(location!=null){
            Text(" Latitude: ${location.latitude}\n" )
            Text("Longitude: ${location.longitude}\n")
            Text("Address: $address")
        }
        else{
            Text("Location not Available")
        }
        
        Button(onClick = {
            if(locationUtils.hasLocationPermission(context)){
                // Permission already granted update location
                locationUtils.requestLocationUpdates(viewModel)
            }
            else{
                // request location
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                
            }

        }) {
            Text(text = "Get Location")
        }

    }
}
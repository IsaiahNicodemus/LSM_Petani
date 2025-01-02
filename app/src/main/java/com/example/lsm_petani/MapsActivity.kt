package com.example.lsm_petani

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null

    private var isLocationButtonClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Mendapatkan referensi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Load Google Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Tombol Ambil Lokasi Saya
        val getLocationButton = findViewById<Button>(R.id.getLocationButton)
        getLocationButton.setOnClickListener {
            getUserLocation()
            isLocationButtonClicked = true
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        // Periksa izin lokasi
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            myMap.isMyLocationEnabled = true
        }

        // Tangkap klik pada peta untuk memilih lokasi
        myMap.setOnMapClickListener { latLng ->
            myMap.clear()
            myMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Lokasi Terpilih")
            )
            selectedLatLng = latLng
        }
    }   

    override fun onBackPressed() {
        if (selectedLatLng != null || isLocationButtonClicked) {
            val intent = Intent().apply {
                putExtra("latitude", selectedLatLng?.latitude ?: 0.0)
                putExtra("longitude", selectedLatLng?.longitude ?: 0.0)
            }
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    private fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)

                    selectedLatLng = userLocation // Simpan lokasi terakhir dari tombol

                    myMap.clear()
                    myMap.addMarker(MarkerOptions().position(userLocation).title("Lokasi Saya"))

                    myMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLocation, 15f),
                        2000,
                        null
                    )
                    Toast.makeText(this, "Lokasi berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

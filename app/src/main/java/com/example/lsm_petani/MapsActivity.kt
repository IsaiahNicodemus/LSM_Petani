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

    // Variabel untuk menyimpan lokasi terakhir
    private var lastSavedLocation: LatLng? = null

    // Untuk izin runtime
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getUserLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Mendapatkan referensi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Load Google Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Hubungkan tombol untuk mengambil lokasi pengguna
        val getLocationButton = findViewById<Button>(R.id.getLocationButton)
        getLocationButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getUserLocation()
            } else {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        // Memeriksa izin lokasi
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            myMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // Jika ada lokasi yang sudah disimpan sebelumnya, tampilkan di peta
        lastSavedLocation?.let { location ->
            myMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
            myMap.addMarker(MarkerOptions().position(location).title("Lokasi Disimpan"))
        }

        // Tangkap klik pada peta untuk memilih lokasi
        myMap.setOnMapClickListener { latLng ->
            myMap.clear() // Membersihkan marker sebelumnya
            myMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Lokasi Terpilih")
            )
            selectedLatLng = latLng // Simpan lokasi yang dipilih
        }
    }


    override fun onBackPressed() {
        if (selectedLatLng != null) {
            // Mengirim hasil lokasi yang dipilih kembali ke fragment pemanggil
            val intent = Intent().apply {
                putExtra("latitude", selectedLatLng!!.latitude)
                putExtra("longitude", selectedLatLng!!.longitude)
            }
            setResult(Activity.RESULT_OK, intent)
        } else {
            // Jika tidak ada lokasi yang dipilih, kirimkan hasil yang di-cancel
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }


    // Mengambil lokasi pengguna
    private fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)

                    // Simpan lokasi terakhir
                    lastSavedLocation = userLocation

                    myMap.clear() // Bersihkan marker sebelumnya
                    myMap.addMarker(MarkerOptions().position(userLocation).title("Lokasi Saya"))

                    // Gunakan animateCamera untuk membuat animasi perpindahan
                    myMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLocation, 15f),
                        2000, // Durasi animasi dalam milidetik
                        null
                    )

                    // Tampilkan pesan bahwa lokasi berhasil disimpan
                    Toast.makeText(this, "Lokasi berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

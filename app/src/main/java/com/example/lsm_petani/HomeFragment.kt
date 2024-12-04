package com.example.lsm_petani

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class HomeFragment : Fragment() {

    private lateinit var ivPhotoPreview: ImageView
    private lateinit var btnUploadPhoto: Button
    private lateinit var etNama: EditText
    private lateinit var btnSelectLocation: Button
    private lateinit var tvSelectedLocation: TextView
    private lateinit var etLuasLahan: EditText
    private lateinit var etNamaPemilik: EditText
    private lateinit var btnSubmit: Button
    private lateinit var etNoHandphone: EditText

    private var photoUri: Uri? = null
    private var selectedLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview)
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto)
        etNama = view.findViewById(R.id.etNama)
        btnSelectLocation = view.findViewById(R.id.btnSelectLocation)
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation)
        etLuasLahan = view.findViewById(R.id.etLuasLahan)
        etNamaPemilik = view.findViewById(R.id.etNamaPemilik)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        etNoHandphone = view.findViewById(R.id.etNoHandphone)

        // Set button actions
        btnUploadPhoto.setOnClickListener { selectPhoto() }
        btnSelectLocation.setOnClickListener { selectLocation() }
        btnSubmit.setOnClickListener { submitForm() }

        return view
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    private fun selectLocation() {
        // Mock implementation for location selection
        selectedLocation = "Latitude: -6.200000, Longitude: 106.816666"
        tvSelectedLocation.text = selectedLocation
    }

    private fun submitForm() {
        val nama = etNama.text.toString()
        val luasLahan = etLuasLahan.text.toString()
        val namaPemilik = etNamaPemilik.text.toString()
        val noHandphone = etNoHandphone.text.toString() // Ambil input No Handphone

        if (nama.isEmpty() || luasLahan.isEmpty() || namaPemilik.isEmpty() || noHandphone.isEmpty() || selectedLocation == null) {
            Toast.makeText(context, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        // Simpan data ke Firebase Realtime Database
        val databaseRef = FirebaseDatabase.getInstance().reference.child("lsm_pertanian").push()
        val data = mapOf(
            "nama" to nama,
            "luasLahan" to luasLahan,
            "namaPemilik" to namaPemilik,
            "noHandphone" to noHandphone, // Field Baru
            "lokasi" to selectedLocation
        )

        databaseRef.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(context, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                // Reset form setelah berhasil disimpan
                resetForm()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetForm() {
        etNama.text.clear()
        etLuasLahan.text.clear()
        etNamaPemilik.text.clear()
        etNoHandphone.text.clear() // Reset No Handphone
        selectedLocation = null
        // Anda bisa menambahkan reset untuk komponen lain jika diperlukan
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data
            ivPhotoPreview.setImageURI(photoUri)
        }
    }
}

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
import com.example.lsm_petani.model.Farmer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.app.AlertDialog

class FarmersFragment : Fragment() {

    private lateinit var ivPhotoPreview: ImageView
    private lateinit var btnUploadPhoto: Button
    private lateinit var etNama: EditText
    private lateinit var btnSelectLocation: Button
    private lateinit var tvSelectedLocation: TextView
    private lateinit var etLuasLahan: EditText
    private lateinit var etNamaPemilik: EditText
    private lateinit var btnSubmit: Button
    private lateinit var etNoHandphone: EditText
    private lateinit var progressBar: ProgressBar

    private var photoUri: Uri? = null
    private var selectedLocation: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_farmers, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
        progressBar = view.findViewById(R.id.progressBar)

        // Set username in EditText
        setUsername()

        // Set button actions
        btnUploadPhoto.setOnClickListener { selectPhoto() }
        btnSelectLocation.setOnClickListener { selectLocation() }
        btnSubmit.setOnClickListener { submitForm() }

        return view
    }

    private fun setUsername() {
        // Ambil nama pengguna dari Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Jika displayName tersedia, set langsung
            val displayName = user.displayName
            if (!displayName.isNullOrEmpty()) {
                etNama.setText(displayName)
                return
            }
        }

        // Jika displayName tidak tersedia, ambil dari database
        val uid = user?.uid
        if (uid != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            databaseRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").value as? String
                etNama.setText(username ?: "Nama tidak tersedia")
            }.addOnFailureListener {
                etNama.setText("Nama tidak tersedia")
            }
        } else {
            etNama.setText("Nama tidak tersedia")
        }
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
        val noHandphone = etNoHandphone.text.toString()

        if (nama.isEmpty() || luasLahan.isEmpty() || namaPemilik.isEmpty() || noHandphone.isEmpty() || selectedLocation == null) {
            showDataDialog("Harap lengkapi semua data.")
            return
        }

        // Tampilkan ProgressBar sebelum memulai kirim data
        progressBar.visibility = View.VISIBLE

        // Menggunakan `push()` hanya untuk data petani, tanpa memasukkan ID pengguna ke struktur
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("lsm_pertanian")
            .push()

        val farmer = Farmer(
            nama = nama,
            lokasi = selectedLocation,
            luasLahan = luasLahan,
            namaPemilik = namaPemilik,
            noHandphone = noHandphone,
            photoUrl = photoUri?.toString(),
            status = false
        )

        databaseRef.setValue(farmer)
            .addOnSuccessListener {
                // Operasi berhasil, sembunyikan ProgressBar dan tampilkan notifikasi dalam dialog
                progressBar.visibility = View.GONE
                showDataDialog("Data Anda berhasil terkirim, silakan tunggu verifikasi admin.")
                resetForm()
            }
            .addOnFailureListener {
                // Operasi gagal, sembunyikan ProgressBar dan tampilkan dialog kesalahan
                progressBar.visibility = View.GONE
                showDataDialog("Gagal mengirim data. Silakan coba lagi.")
            }
    }


    private fun showDataDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Informasi")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun resetForm() {
        etNama.text.clear()
        etLuasLahan.text.clear()
        etNamaPemilik.text.clear()
        etNoHandphone.text.clear()
        ivPhotoPreview.setImageResource(0)
        tvSelectedLocation.text = ""
        selectedLocation = null
        photoUri = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data
            ivPhotoPreview.setImageURI(photoUri)
        }
    }
}

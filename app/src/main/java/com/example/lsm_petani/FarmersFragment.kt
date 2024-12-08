package com.example.lsm_petani

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.util.*

class FarmersFragment : Fragment() {

    private lateinit var ivPhotoPreview: ImageView
    private lateinit var btnUploadPhoto: Button
    private lateinit var etNama: EditText
    private lateinit var btnSelectLocation: Button
    private lateinit var tvSelectedLocation: TextView
    private lateinit var etLuasLahan: EditText
    private lateinit var etNamaPemilik: EditText
    private lateinit var etHargaPerMeter: EditText
    private lateinit var btnSelectDateTime: Button
    private lateinit var tvSelectedDateTime: TextView
    private lateinit var btnSubmit: Button
    private lateinit var etNoHandphone: EditText
    private lateinit var progressBar: ProgressBar

    private var photoUri: Uri? = null
    private var selectedLocation: String? = null
    private var selectedDateTime: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_farmers, container, false)

        auth = FirebaseAuth.getInstance()
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview)
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto)
        etNama = view.findViewById(R.id.etNama)
        btnSelectLocation = view.findViewById(R.id.btnSelectLocation)
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation)
        etLuasLahan = view.findViewById(R.id.etLuasLahan)
        etNamaPemilik = view.findViewById(R.id.etNamaPemilik)
        etHargaPerMeter = view.findViewById(R.id.etHargaPerMeter)
        btnSelectDateTime = view.findViewById(R.id.btnSelectDateTime)
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        etNoHandphone = view.findViewById(R.id.etNoHandphone)
        progressBar = view.findViewById(R.id.progressBar)

        btnUploadPhoto.setOnClickListener { selectPhoto() }
        btnSelectLocation.setOnClickListener { selectLocation() }
        btnSelectDateTime.setOnClickListener { selectDateTime() }
        btnSubmit.setOnClickListener { submitForm() }

        return view
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    private fun selectLocation() {
        selectedLocation = "Latitude: -6.200000, Longitude: 106.816666"
        tvSelectedLocation.text = selectedLocation
    }

    private fun selectDateTime() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedDateTime = "$dayOfMonth/${month + 1}/$year $hour:$minute"
                tvSelectedDateTime.text = selectedDateTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun submitForm() {
        val nama = etNama.text.toString()
        val luasLahan = etLuasLahan.text.toString()
        val namaPemilik = etNamaPemilik.text.toString()
        val noHandphone = etNoHandphone.text.toString()
        val hargaPerMeter = etHargaPerMeter.text.toString()

        if (nama.isEmpty() || luasLahan.isEmpty() || namaPemilik.isEmpty() ||
            noHandphone.isEmpty() || hargaPerMeter.isEmpty() || selectedLocation == null || selectedDateTime == null
        ) {
            showDataDialog("Harap lengkapi semua data.")
            return
        }

        progressBar.visibility = View.VISIBLE
        val databaseRef = FirebaseDatabase.getInstance().reference.child("lsm_pertanian").push()
        val farmer = Farmer(
            nama = nama,
            lokasi = selectedLocation,
            luasLahan = luasLahan,
            namaPemilik = namaPemilik,
            noHandphone = noHandphone,
            pricePerMeter = hargaPerMeter.toDoubleOrNull(), // Pastikan hargaPerMeter valid
            timestamp = System.currentTimeMillis(), // Jika perlu, tambahkan timestamp
            photoUrl = photoUri?.toString(), // Perbaiki ini menjadi photoUrl
            status = false // Ini adalah Boolean yang benar
        )


        databaseRef.setValue(farmer)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                showDataDialog("Data berhasil dikirim.")
                resetForm()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showDataDialog("Gagal mengirim data.")
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
        etHargaPerMeter.text.clear()
        ivPhotoPreview.setImageResource(0)
        tvSelectedLocation.text = ""
        tvSelectedDateTime.text = "Tanggal & Waktu belum dipilih"
        selectedLocation = null
        selectedDateTime = null
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

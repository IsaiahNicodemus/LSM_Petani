package com.example.lsm_petani

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.lsm_petani.model.Farmer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
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

        setUsername()
        setupPriceFormatter()
        setupAreaFormatter()

        btnUploadPhoto.setOnClickListener { selectPhoto() }
        btnSelectLocation.setOnClickListener { selectLocation() }
        btnSelectDateTime.setOnClickListener { selectDateTime() }
        btnSubmit.setOnClickListener { submitForm() }

        return view
    }

    private fun setUsername() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userUid = user.uid
            val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(userUid)

            databaseRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").value as? String
                if (!username.isNullOrEmpty()) {
                    etNama.setText(username)
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil data nama pengguna.", Toast.LENGTH_SHORT).show()
            }
        } else {
            etNama.setText("") // Jika pengguna tidak ditemukan, kosongkan input
        }
    }

    private fun setupPriceFormatter() {
        etHargaPerMeter.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                try {
                    val input = s.toString().replace("[Rp,.]".toRegex(), "").toDoubleOrNull()
                    val formatted = if (input != null) "Rp. ${String.format("%,.0f", input)}" else ""
                    etHargaPerMeter.setText(formatted)
                    etHargaPerMeter.setSelection(formatted.length) // Pindahkan kursor ke akhir
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                isEditing = false
            }
        })
    }

    private fun setupAreaFormatter() {
        etLuasLahan.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                isEditing = true

                try {
                    val input = s.toString().replace(",", "").replace(" m²", "").toDoubleOrNull()
                    val formatted = if (input != null) {
                        "${String.format("%,.0f", input)} m²"
                    } else {
                        ""
                    }
                    etLuasLahan.setText(formatted)
                    etLuasLahan.setSelection(formatted.length) // Pindahkan kursor ke akhir
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                isEditing = false
            }
        })
    }

    private fun selectPhoto() {
        val options = arrayOf("Pilih dari Galeri", "Ambil Foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Unggah Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Pilih dari Galeri
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, 101)
                    }
                    1 -> { // Ambil Foto
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, 102)
                    }
                }
            }
            .show()
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
        val nama = etNama.text.toString().trim()
        val luasLahan = etLuasLahan.text.toString().replace(" m²", "").replace(",", "").trim()
        val namaPemilik = etNamaPemilik.text.toString().trim()
        val noHandphone = etNoHandphone.text.toString().trim()
        val hargaPerMeter = etHargaPerMeter.text.toString().replace("Rp. ", "").replace(",", "").trim()

        if (nama.isEmpty() || luasLahan.isEmpty() || namaPemilik.isEmpty() ||
            noHandphone.isEmpty() || hargaPerMeter.isEmpty() || selectedLocation == null || selectedDateTime == null
        ) {
            showDataDialog("Harap lengkapi semua data.")
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().reference.child("lsm_pertanian").push()
        val farmer = Farmer(
            nama = nama,
            lokasi = selectedLocation,
            luasLahan = luasLahan,
            namaPemilik = namaPemilik,
            noHandphone = noHandphone,
            pricePerMeter = hargaPerMeter.toDoubleOrNull(), // Parsing harga menjadi Double
            timestamp = System.currentTimeMillis(), // Tambahkan timestamp jika perlu
            photoUrl = photoUri?.toString(), // URL foto
            status = false // Status default
        )

        progressBar.visibility = View.VISIBLE
        databaseRef.setValue(farmer)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                showDataDialog("Data Anda berhasil terkirim, silakan tunggu verifikasi admin.")
                resetForm()
            }
            .addOnFailureListener {
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
        etHargaPerMeter.text.clear()
        ivPhotoPreview.setImageResource(0)
        tvSelectedLocation.text = ""
        tvSelectedDateTime.text = "Tanggal & Waktu belum dipilih"
        selectedLocation = null
        selectedDateTime = null
        photoUri = null
    }

    private fun saveBitmapToUri(bitmap: android.graphics.Bitmap?): Uri? {
        return if (bitmap != null) {
            val file = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            101 -> { // Dari galeri
                if (resultCode == Activity.RESULT_OK) {
                    photoUri = data?.data
                    ivPhotoPreview.setImageURI(photoUri)
                }
            }
            102 -> { // Dari kamera
                if (resultCode == Activity.RESULT_OK) {
                    val photoBitmap = data?.extras?.get("data") as? android.graphics.Bitmap
                    ivPhotoPreview.setImageBitmap(photoBitmap)
                    // Simpan bitmap sebagai URI untuk diunggah jika diperlukan
                    photoUri = saveBitmapToUri(photoBitmap)
                }
            }
        }
    }

}

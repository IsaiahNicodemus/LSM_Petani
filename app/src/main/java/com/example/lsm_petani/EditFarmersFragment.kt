package com.example.lsm_petani

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.lsm_petani.model.Farmer
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EditFarmersFragment : Fragment() {

    private lateinit var etFarmerName: EditText
    private lateinit var etFarmerLocation: EditText
    private lateinit var etFarmerArea: EditText
    private lateinit var etFarmerOwner: EditText
    private lateinit var etFarmerPhone: EditText
    private lateinit var etFarmerPricePerMeter: EditText
    private lateinit var tvFarmerDate: TextView
    private lateinit var ivFarmerPhoto: ImageView
    private lateinit var btnSelectPhoto: Button
    private var farmerKey: String? = null
    private var selectedDate: Long? = null
    private var selectedPhotoUrl: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_farmers, container, false)

        // Initialize UI components
        etFarmerName = view.findViewById(R.id.etFarmerName)
        etFarmerLocation = view.findViewById(R.id.etFarmerLocation)
        etFarmerArea = view.findViewById(R.id.etFarmerArea)
        etFarmerOwner = view.findViewById(R.id.etFarmerOwner)
        etFarmerPhone = view.findViewById(R.id.etFarmerPhone)
        etFarmerPricePerMeter = view.findViewById(R.id.etFarmerPricePerMeter)
        tvFarmerDate = view.findViewById(R.id.tvFarmerDate)
        ivFarmerPhoto = view.findViewById(R.id.ivFarmerPhoto)
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto)

        btnSelectPhoto.setOnClickListener { selectPhoto() }
        val btnPickDate: Button = view.findViewById(R.id.btnPickDate)
        btnPickDate.setOnClickListener { showDatePicker() }

        // Get data from arguments
        val bundle = arguments
        bundle?.let {
            farmerKey = it.getString("farmer_key")
            val farmerName = it.getString("farmer_name")
            val farmerLocation = it.getString("farmer_location")
            val farmerArea = it.getString("farmer_area")?.toDoubleOrNull() ?: 0.0
            val farmerOwner = it.getString("farmer_owner")
            val farmerPhone = it.getString("farmer_phone")
            val farmerPhotoUrl = it.getString("farmer_photo_url")
            val farmerPricePerMeter = it.getDouble("farmer_price_per_meter", 0.0)
            val farmerDate = it.getLong("farmer_date", System.currentTimeMillis())

            // Populate fields
            etFarmerName.setText(farmerName ?: "")
            etFarmerLocation.setText(farmerLocation ?: "")
            etFarmerArea.setText(formatArea(farmerArea))
            etFarmerOwner.setText(farmerOwner ?: "")
            etFarmerPhone.setText(farmerPhone ?: "")
            etFarmerPricePerMeter.setText(formatCurrency(farmerPricePerMeter))
            selectedDate = farmerDate
            updateDateText(farmerDate)

            Glide.with(this)
                .load(farmerPhotoUrl ?: R.drawable.ic_image_placeholder)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(ivFarmerPhoto)
        }

        // Save button
        val btnSave: Button = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener { saveFarmerData() }

        return view
    }

    private fun formatArea(area: Double): String {
        val formatter = NumberFormat.getInstance(Locale.US)
        return "${formatter.format(area)} m²"
    }

    // Function to format price to currency properly
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getInstance(Locale.US)
        return "Rp. ${formatter.format(price)}"
    }

    private fun selectPhoto() {
        val options = arrayOf("Pilih dari Galeri", "Ambil Foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Unggah Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Galeri
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, 101)
                    }
                    1 -> { // Kamera
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, 102)
                    }
                }
            }
            .show()
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.timeInMillis
                updateDateText(selectedDate!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateText(dateInMillis: Long) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvFarmerDate.text = formatter.format(dateInMillis)
    }

    private fun saveFarmerData() {
        val name = etFarmerName.text.toString()
        val location = etFarmerLocation.text.toString()
        val area = etFarmerArea.text.toString().replace(" m²", "").replace(",", "").toDoubleOrNull() ?: 0.0
        val owner = etFarmerOwner.text.toString()
        val phone = etFarmerPhone.text.toString()
        val pricePerMeter = etFarmerPricePerMeter.text.toString().replace("Rp. ", "").replace(",", "").toDoubleOrNull() ?: 0.0

        if (farmerKey == null || selectedDate == null || selectedPhotoUrl == null) {
            Toast.makeText(context, "Data petani tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedFarmer = Farmer(
            key = farmerKey,
            nama = name,
            lokasi = location,
            luasLahan = area.toString(),
            namaPemilik = owner,
            noHandphone = phone,
            photoUrl = selectedPhotoUrl, // Path dari fungsi `savePhotoToCache`
            pricePerMeter = pricePerMeter,
            timestamp = selectedDate
        )

        updateFarmerData(updatedFarmer)
    }



    private fun updateFarmerData(farmer: Farmer) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("lsm_pertanian")
        farmer.key?.let {
            databaseRef.child(it).setValue(farmer)
                .addOnSuccessListener {
                    Toast.makeText(context, "Data petani berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal memperbarui data petani", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun savePhotoToCache(bitmap: android.graphics.Bitmap) {
        try {
            val cacheDir = requireContext().cacheDir
            val photoFile = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val outputStream = photoFile.outputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            selectedPhotoUrl = photoFile.absolutePath
            Glide.with(this).load(photoFile).into(ivFarmerPhoto) // Load ke ImageView
            Toast.makeText(context, "Foto berhasil disimpan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            when (requestCode) {
                101 -> { // Photo dari galeri
                    selectedPhotoUrl = data.data.toString()
                    Glide.with(this).load(selectedPhotoUrl).into(ivFarmerPhoto)
                }
                102 -> { // Ambil foto dari kamera
                    val photoBitmap = data.extras?.get("data") as? android.graphics.Bitmap
                    if (photoBitmap != null) {
                        savePhotoToCache(photoBitmap)
                    }
                }
            }
        }
    }



}

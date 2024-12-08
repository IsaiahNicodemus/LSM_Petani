package com.example.lsm_petani

import android.app.DatePickerDialog
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
    private var farmerKey: String? = null
    private var selectedDate: Long? = null

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

        val btnPickDate: Button = view.findViewById(R.id.btnPickDate)
        btnPickDate.setOnClickListener { showDatePicker() }

        // Get data from arguments
        val bundle = arguments
        bundle?.let {
            farmerKey = it.getString("farmer_key")
            val farmerName = it.getString("farmer_name")
            val farmerLocation = it.getString("farmer_location")
            val farmerArea = it.getString("farmer_area")
            val farmerOwner = it.getString("farmer_owner")
            val farmerPhone = it.getString("farmer_phone")
            val farmerPhotoUrl = it.getString("farmer_photo_url")
            val farmerPricePerMeter = it.getDouble("farmer_price_per_meter", 0.0)
            val farmerDate = it.getLong("farmer_date", System.currentTimeMillis())

            // Populate fields
            etFarmerName.setText(farmerName ?: "")
            etFarmerLocation.setText(farmerLocation ?: "")
            etFarmerArea.setText(farmerArea ?: "")
            etFarmerOwner.setText(farmerOwner ?: "")
            etFarmerPhone.setText(farmerPhone ?: "")
            etFarmerPricePerMeter.setText(farmerPricePerMeter.toString())
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
        val area = etFarmerArea.text.toString()
        val owner = etFarmerOwner.text.toString()
        val phone = etFarmerPhone.text.toString()
        val pricePerMeter = etFarmerPricePerMeter.text.toString().toDoubleOrNull() ?: 0.0

        // Mendapatkan photoUrl yang ada dari bundle atau biarkan null jika tidak ada perubahan
        val photoUrl = arguments?.getString("farmer_photo_url")

        if (farmerKey == null || selectedDate == null) {
            Toast.makeText(context, "Data petani tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedFarmer = Farmer(
            key = farmerKey,
            nama = name,
            lokasi = location,
            luasLahan = area,
            namaPemilik = owner,
            noHandphone = phone,
            photoUrl = photoUrl, // Gunakan URL foto yang diterima
            pricePerMeter = pricePerMeter,
            timestamp = selectedDate // Save selected date
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
}

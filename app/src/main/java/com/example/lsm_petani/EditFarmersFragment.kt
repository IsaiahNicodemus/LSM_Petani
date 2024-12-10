package com.example.lsm_petani

import android.app.Activity
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditFarmersFragment : Fragment() {

    private lateinit var etFarmerName: EditText
    private lateinit var tvSelectedLocation: TextView
    private lateinit var etFarmerArea: EditText
    private lateinit var etFarmerOwner: EditText
    private lateinit var etFarmerPhone: EditText
    private lateinit var etFarmerPricePerMeter: EditText
    private lateinit var ivFarmerPhoto: ImageView
    private var selectedDate: Long? = null
    private var farmerKey: String? = null
    private var selectedLocation: String? = null
    private val REQUEST_CODE_MAPS = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_farmers, container, false)
        val tvFarmerDate: TextView = view.findViewById(R.id.tvFarmerDate)
        val btnPickDate: Button = view.findViewById(R.id.btnPickDate)

        btnPickDate.setOnClickListener {
            showDatePicker(tvFarmerDate)
        }

        // Inisialisasi View
        etFarmerName = view.findViewById(R.id.etFarmerName)
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation)
        etFarmerArea = view.findViewById(R.id.etFarmerArea)
        etFarmerOwner = view.findViewById(R.id.etFarmerOwner)
        etFarmerPhone = view.findViewById(R.id.etFarmerPhone)
        etFarmerPricePerMeter = view.findViewById(R.id.etFarmerPricePerMeter)
        ivFarmerPhoto = view.findViewById(R.id.ivFarmerPhoto)

        val btnSelectLocation: Button = view.findViewById(R.id.btnSelectLocation)
        btnSelectLocation.setOnClickListener { openMapsActivity() }

        // Tombol Save
        val btnSave: Button = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener { saveFarmerData() }

        // Get data dari argument
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

            // Isi data ke view
            etFarmerName.setText(farmerName ?: "")
            tvSelectedLocation.text = farmerLocation ?: "Lokasi belum dipilih"
            etFarmerArea.setText(farmerArea)
            etFarmerOwner.setText(farmerOwner ?: "")
            etFarmerPhone.setText(farmerPhone ?: "")
            etFarmerPricePerMeter.setText(formatCurrency(farmerPricePerMeter))

            Glide.with(this)
                .load(farmerPhotoUrl ?: R.drawable.ic_image_placeholder)
                .into(ivFarmerPhoto)

            selectedLocation = farmerLocation
        }

        return view
    }

    private fun updateDateText(tvDate: TextView, dateInMillis: Long) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvDate.text = formatter.format(Date(dateInMillis))
    }

    private fun showDatePicker(tvDate: TextView) {
        val calendar = Calendar.getInstance()
        selectedDate?.let { calendar.timeInMillis = it }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Perbarui tanggal yang dipilih
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                // Perbarui tampilan tanggal
                updateDateText(tvDate, selectedDate!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun openMapsActivity() {
        val intent = Intent(requireContext(), MapsActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_MAPS)
    }

    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getInstance(Locale.US)
        return "Rp. ${formatter.format(price)}"
    }

    private fun saveFarmerData() {
        val name = etFarmerName.text.toString()
        val location = selectedLocation ?: tvSelectedLocation.text.toString()
        val area = etFarmerArea.text.toString().replace(" m²", "").toDoubleOrNull() ?: 0.0
        val owner = etFarmerOwner.text.toString()
        val phone = etFarmerPhone.text.toString()
        val pricePerMeter = etFarmerPricePerMeter.text.toString().replace("Rp. ", "").toDoubleOrNull() ?: 0.0
        val photoUrl = "url_to_photo" // Use the URL of the selected photo if you implement photo upload logic

        if (farmerKey == null || selectedDate == null) {
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
            photoUrl = photoUrl,
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

                    // Pindah ke HomeFragment menggunakan FragmentTransaction
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, HomeFragment())  // Ganti dengan ID container yang sesuai
                    transaction.addToBackStack(null)  // Menambahkan ke back stack (opsional)
                    transaction.commit()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal memperbarui data petani", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAPS && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            if (latitude != null && longitude != null) {
                selectedLocation = "Lat: $latitude, Long: $longitude"
                tvSelectedLocation.text = selectedLocation
            } else {
                Toast.makeText(requireContext(), "Lokasi belum dipilih.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

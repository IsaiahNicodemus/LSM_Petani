package com.example.lsm_petani

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lsm_petani.model.Farmer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FarmersAdapter(
    private val farmers: List<Farmer>,
    private val isAdmin: Boolean,
    private val onEditClicked: (Farmer) -> Unit,
    private val onDeleteClicked: (Farmer) -> Unit
) : RecyclerView.Adapter<FarmersAdapter.FarmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_farmer, parent, false)
        return FarmerViewHolder(view)
    }

    override fun getItemCount(): Int = farmers.size

    override fun onBindViewHolder(holder: FarmerViewHolder, position: Int) {
        val farmer = farmers[position]
        holder.bind(farmer)
    }

    inner class FarmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFarmerPhoto: ImageView = itemView.findViewById(R.id.ivFarmerPhoto)
        private val tvName: TextView = itemView.findViewById(R.id.tvFarmerName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvFarmerLocation)
        private val tvArea: TextView = itemView.findViewById(R.id.tvFarmerArea)
        private val tvOwner: TextView = itemView.findViewById(R.id.tvFarmerOwner)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvFarmerPhone)
        private val tvPricePerMeter: TextView = itemView.findViewById(R.id.tvFarmerPricePerMeter)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvFarmerTimestamp)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvFarmerStatus)
        private val btnToggleStatus: Button = itemView.findViewById(R.id.btnToggleStatus)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(farmer: Farmer) {
            tvName.text = farmer.nama
            tvLocation.text = farmer.lokasi
            tvArea.text = "Luas: ${farmer.luasLahan} m²"
            tvOwner.text = "Pemilik: ${farmer.namaPemilik}"
            tvPhone.text = "No HP: ${farmer.noHandphone}"
            tvPricePerMeter.text = "Harga/m²: Rp ${farmer.pricePerMeter?.let { String.format("%,.0f", it) } ?: "0"}"

            tvTimestamp.text = farmer.timestamp?.toLong()?.let {
                "Tanggal: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))}"
            } ?: "Tanggal: N/A"

            tvStatus.text = if (farmer.status) "Status: Aktif" else "Status: Menunggu Verifikasi"
            loadFarmerImage(farmer.photoUrl)

            btnEdit.setOnClickListener { onEditClicked(farmer) }
            btnDelete.setOnClickListener { onDeleteClicked(farmer) }
            btnToggleStatus.setOnClickListener { toggleStatus(farmer) }

            if (isAdmin) {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
                btnToggleStatus.visibility = View.VISIBLE
                btnToggleStatus.text = if (farmer.status) "Tandai Tidak Aktif" else "Verifikasi Petani"
            } else {
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
                btnToggleStatus.visibility = View.GONE
            }
        }

        private fun loadFarmerImage(photoUrl: String?) {
            Glide.with(itemView.context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(ivFarmerPhoto)
        }

        private fun toggleStatus(farmer: Farmer) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("lsm_pertanian").child(farmer.key!!)
            val newStatus = !farmer.status
            databaseRef.child("status").setValue(newStatus).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(itemView.context, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(itemView.context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun updateRoleToFarmer(userId: String?) {
            if (userId.isNullOrEmpty()) return

            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.child("role").setValue("Petani").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(itemView.context, "Role berhasil diperbarui menjadi Petani", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(itemView.context, "Gagal memperbarui role", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

package com.example.lsm_petani

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lsm_petani.model.Farmer
import com.google.firebase.database.FirebaseDatabase

class FarmersAdapter(
    private val farmers: List<Farmer>,
    private val isAdmin: Boolean
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

            val statusText = if (farmer.status) "Aktif" else "Menunggu diverifikasi"
            tvStatus.text = "Status: $statusText"

            if (isAdmin) {
                btnToggleStatus.visibility = View.VISIBLE
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            } else {
                btnToggleStatus.visibility = View.GONE
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(farmer.photoUrl ?: R.drawable.ic_image_placeholder)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(ivFarmerPhoto)

            btnToggleStatus.setOnClickListener { toggleStatus(farmer) }
            btnEdit.setOnClickListener {
                Toast.makeText(itemView.context, "Edit clicked untuk ${farmer.nama}", Toast.LENGTH_SHORT).show()
            }
            btnDelete.setOnClickListener {
                Toast.makeText(itemView.context, "Delete clicked untuk ${farmer.nama}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun toggleStatus(farmer: Farmer) {
            val key = farmer.key ?: return
            val databaseRef = FirebaseDatabase.getInstance().getReference("lsm_pertanian").child(key)
            val newStatus = !farmer.status

            databaseRef.child("status").setValue(newStatus).addOnSuccessListener {
                Toast.makeText(itemView.context, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                tvStatus.text = "Status: ${if (newStatus) "Aktif" else "Menunggu diverifikasi"}"
            }.addOnFailureListener {
                Toast.makeText(itemView.context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

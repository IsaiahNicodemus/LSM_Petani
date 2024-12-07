package com.example.lsm_petani

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.lsm_petani.model.Farmer
import com.google.firebase.database.FirebaseDatabase

class FarmersAdapter(private val farmerList: ArrayList<Farmer>, private val isAdmin: Boolean) :
    RecyclerView.Adapter<FarmersAdapter.FarmerViewHolder>() {

    class FarmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivFarmerPhoto)
        val tvName: TextView = itemView.findViewById(R.id.tvFarmerName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvFarmerLocation)
        val tvArea: TextView = itemView.findViewById(R.id.tvFarmerArea)
        val tvOwner: TextView = itemView.findViewById(R.id.tvFarmerOwner)
        val tvPhone: TextView = itemView.findViewById(R.id.tvFarmerPhone)
        val btnToggleStatus: Button = itemView.findViewById(R.id.btnToggleStatus)
        val tvFarmerStatus: TextView = itemView.findViewById(R.id.tvFarmerStatus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_farmer, parent, false)
        return FarmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FarmerViewHolder, position: Int) {
        val farmer = farmerList[position]
        holder.tvName.text = farmer.nama
        holder.tvLocation.text = farmer.lokasi
        holder.tvArea.text = "Luas: ${farmer.luasLahan} m²"
        holder.tvOwner.text = "Pemilik: ${farmer.namaPemilik}"
        holder.tvPhone.text = "No HP: ${farmer.noHandphone}"

        // Atur status dinamis
        val statusText = if (farmer.status) "Status : Aktif" else "Status : Menunggu diverifikasi"
        val tvFarmerStatus: TextView = holder.itemView.findViewById(R.id.tvFarmerStatus)
        tvFarmerStatus.text = statusText

        if (isAdmin) {
            holder.btnToggleStatus.visibility = View.VISIBLE
            holder.btnToggleStatus.setOnClickListener {
                toggleStatus(farmer, holder)
            }
        } else {
            holder.btnToggleStatus.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return farmerList.size
    }

    private fun toggleStatus(farmer: Farmer, holder: FarmerViewHolder) {
        val key = farmer.key ?: return // Pastikan key tidak null
        val databaseRef = FirebaseDatabase.getInstance().getReference("lsm_pertanian").child(key)
        val newStatus = !farmer.status // Toggle status

        databaseRef.child("status").setValue(newStatus).addOnSuccessListener {
            val statusText = if (newStatus) "Status : Aktif" else "Status : Menunggu diverifikasi"
            holder.btnToggleStatus.text = statusText
            Toast.makeText(holder.itemView.context, "Status berhasil diperbarui.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(holder.itemView.context, "Gagal memperbarui status.", Toast.LENGTH_SHORT).show()
        }
    }
}

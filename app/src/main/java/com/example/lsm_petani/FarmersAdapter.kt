package com.example.lsm_petani

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lsm_petani.model.Farmer

class FarmersAdapter(private val farmerList: ArrayList<Farmer>) :
    RecyclerView.Adapter<FarmersAdapter.FarmerViewHolder>() {

    class FarmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivFarmerPhoto)
        val tvName: TextView = itemView.findViewById(R.id.tvFarmerName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvFarmerLocation)
        val tvArea: TextView = itemView.findViewById(R.id.tvFarmerArea)
        val tvOwner: TextView = itemView.findViewById(R.id.tvFarmerOwner)
        val tvPhone: TextView = itemView.findViewById(R.id.tvFarmerPhone) // Field Baru
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
        holder.tvPhone.text = "No HP: ${farmer.noHandphone}" // Menampilkan No Handphone
    }

    override fun getItemCount(): Int {
        return farmerList.size
    }
}

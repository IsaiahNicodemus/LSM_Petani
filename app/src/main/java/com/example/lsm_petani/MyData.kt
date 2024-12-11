package com.example.lsm_petani

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lsm_petani.model.Farmer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyData : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var farmerAdapter: FarmersAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val farmersList = mutableListOf<Farmer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_data, container, false)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("lsm_pertanian")

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val isAdminUser = auth.currentUser?.email == "admin@example.com" // Logika admin
        farmerAdapter = FarmersAdapter(farmersList, isAdminUser, ::onEditClicked, ::onDeleteClicked)
        recyclerView.adapter = farmerAdapter

        fetchFarmers()

        return view
    }

    private fun fetchFarmers() {
        val currentUserId = auth.currentUser?.uid ?: return
        databaseRef.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    farmersList.clear()
                    for (data in snapshot.children) {
                        val farmer = data.getValue(Farmer::class.java)
                        if (farmer != null) {
                            farmersList.add(farmer)
                        }
                    }
                    farmerAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal mengambil data.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onEditClicked(farmer: Farmer) {
        // Arahkan ke halaman edit atau tampilkan dialog edit
        Toast.makeText(requireContext(), "Edit: ${farmer.nama}", Toast.LENGTH_SHORT).show()
    }

    private fun onDeleteClicked(farmer: Farmer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setPositiveButton("Hapus") { _, _ ->
                farmer.key?.let { key ->
                    databaseRef.child(key).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Data berhasil dihapus.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Gagal menghapus data.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

package com.example.lsm_petani

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lsm_petani.model.Farmer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var farmerList: ArrayList<Farmer>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fab: FloatingActionButton

    private var isAdmin: Boolean = true // Default bukan admin

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFarmers)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        fab = view.findViewById(R.id.fab)
        auth = FirebaseAuth.getInstance()
        farmerList = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("lsm_pertanian")

        checkUserRole() // Cek apakah user admin atau petani
        fetchFarmersData()

        fab.setOnClickListener {
            if (isAdmin) {
                Toast.makeText(context, "Admin dapat mengupdate status petani.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Anda hanya bisa melihat data.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").value?.toString()
                        if (role == "Admin") {
                            isAdmin = true
                            Toast.makeText(context, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                        } else {
                            isAdmin = false
                            Toast.makeText(context, "Login sebagai Petani", Toast.LENGTH_SHORT).show()
                        }
                        fetchFarmersData() // Fetch data setelah role diketahui
                    } else {
                        Toast.makeText(context, "Pengguna tidak ditemukan di database!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal memeriksa role: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchFarmersData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                farmerList.clear()
                for (data in snapshot.children) {
                    val farmer = data.getValue(Farmer::class.java)?.copy(key = data.key)
                    if (farmer != null) {
                        if (isAdmin || farmer.status) { // Admin melihat semua, Petani hanya status = true
                            farmerList.add(farmer)
                        }
                    }
                }

                // Periksa jika data kosong
                if (farmerList.isEmpty()) {
                    Toast.makeText(context, "Data tidak tersedia!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Data berhasil dimuat: ${farmerList.size} item", Toast.LENGTH_SHORT).show()
                }

                // Tampilkan data ke RecyclerView
                val adapter = FarmersAdapter(farmerList, isAdmin)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}

package com.example.lsm_petani

import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.lsm_petani.tutorial.NavigationActivity

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var farmerList: ArrayList<Farmer>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fab: FloatingActionButton

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
        database = FirebaseDatabase.getInstance().getReference("lsm_pertanian") // Path database

        fetchFarmersData()

        fab.setOnClickListener {
            checkUserRole()
        }

        return view
    }

    private fun fetchFarmersData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                farmerList.clear()
                for (data in snapshot.children) {
                    val farmer = data.getValue(Farmer::class.java)
                    if (farmer != null) {
                        farmerList.add(farmer)
                    }
                }
                val adapter = FarmersAdapter(farmerList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").value.toString()
                if (role == "Petani") {
                    Toast.makeText(context, "Anda sudah terverifikasi sebagai Petani.", Toast.LENGTH_SHORT).show()
                } else {
                    showVerificationDialog()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Gagal memeriksa peran pengguna.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showVerificationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Oops...")
            .setMessage("Anda harus memverifikasi kalau Anda petani.")
            .setPositiveButton("Verifikasi") { _, _ ->
                val intent = Intent(context, NavigationActivity::class.java)
                intent.putExtra("navigateTo", "FarmersFragment")
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

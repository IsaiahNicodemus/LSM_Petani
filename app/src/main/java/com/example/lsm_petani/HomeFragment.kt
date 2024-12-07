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
import com.example.lsm_petani.tutorial.NavigationActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var farmerList: ArrayList<Farmer>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fab: FloatingActionButton

    private var hasShownRoleDialog = false
    private var hasShownDataDialog = false

    private lateinit var btnEdit: FloatingActionButton
    private lateinit var btnDelete: FloatingActionButton

    private var isAdmin: Boolean = false // Default bukan admin

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
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val role = snapshot.child("role").value?.toString()
                            if (role == "Admin" || role == "Petani") {
                                // Admin dan Petani dapat menambah data
                                val intent = Intent(context, NavigationActivity::class.java)
                                intent.putExtra("navigateTo", "FarmersFragment")
                                startActivity(intent)
                            } else {
                                // Selain Admin dan Petani, tampilkan dialog verifikasi
                                showVerificationDialog()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Pengguna tidak ditemukan di database!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            context,
                            "Gagal memeriksa role: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                Toast.makeText(context, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
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

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").value?.toString()
                        val mainActivity = activity as MainActivity
                        if (role == "Admin") {
                            isAdmin = true
                            if (!mainActivity.hasShownRoleDialog) {
                                showRoleDialog("Login sebagai Admin")
                                mainActivity.hasShownRoleDialog = true
                            }
                        } else if (role == "Petani") {
                            isAdmin = false
                            if (!mainActivity.hasShownRoleDialog) {
                                showRoleDialog("Login sebagai Petani")
                                mainActivity.hasShownRoleDialog = true
                            }
                        } else {
                            if (!mainActivity.hasShownRoleDialog) {
                                showRoleDialog("Login sebagai User")
                                mainActivity.hasShownRoleDialog = true
                            }
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

                val mainActivity = activity as MainActivity
                if (farmerList.isEmpty()) {
                    if (!mainActivity.hasShownDataDialog) {
                        showDataDialog("Data tidak tersedia!")
                        mainActivity.hasShownDataDialog = true
                    }
                } else {
                    if (!mainActivity.hasShownDataDialog) {
                        showDataDialog("Data berhasil dimuat: ${farmerList.size} item")
                        mainActivity.hasShownDataDialog = true
                    }
                }

                val adapter = FarmersAdapter(farmerList, isAdmin)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRoleDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Informasi Login")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDataDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Informasi Data")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}

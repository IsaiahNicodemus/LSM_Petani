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
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var farmerList: ArrayList<Farmer>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fab: FloatingActionButton
    private var isAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFarmers)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener { navigateToAddFarmer() }

        auth = FirebaseAuth.getInstance()
        farmerList = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("lsm_pertanian")

        checkUserRole()
        return view
    }

    private fun navigateToAddFarmer() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").value?.toString()
                        when (role) {
                            "User" -> {
                                // Tampilkan dialog verifikasi terlebih dahulu
                                showVerificationDialog {
                                    // Jika dialog dikonfirmasi, navigasikan ke NavigationActivity
                                    val intent = Intent(context, NavigationActivity::class.java)
                                    intent.putExtra("navigateTo", "FarmersFragment")
                                    startActivity(intent)
                                }
                            }
                            "Admin", "Petani" -> {
                                // Jika peran adalah "Admin" atau "Petani", langsung ke MainActivity dan navigasikan ke FarmersFragment
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.putExtra("navigateTo", "FarmersFragment")
                                startActivity(intent)
                                requireActivity().finish() // Tutup aktivitas saat ini
                            }
                            else -> {
                                showVerificationDialog()
                            }
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

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").value?.toString()
                        isAdmin = role == "Admin"
                        fetchFarmersData()
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

    private fun fetchFarmersData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                farmerList.clear()
                for (data in snapshot.children) {
                    val farmer = data.getValue(Farmer::class.java)?.copy(key = data.key)
                    if (farmer != null) {
                        if (isAdmin || farmer.status) {
                            farmerList.add(farmer)
                        }
                    }
                }

                if (farmerList.isEmpty()) {
                    showInfoDialog("Tidak ada data petani yang tersedia.")
                }

                val adapter = FarmersAdapter(farmerList, isAdmin, { farmer ->
                    // Handle edit action
                    val bundle = Bundle().apply {
                        putString("farmer_key", farmer.key)
                        putString("farmer_name", farmer.nama)
                        putString("farmer_location", farmer.lokasi)
                        putString("farmer_area", farmer.luasLahan)
                        putString("farmer_owner", farmer.namaPemilik)
                        putString("farmer_phone", farmer.noHandphone)
                        putString("farmer_photo_url", farmer.photoUrl)
                        putString("farmer_user_id", farmer.userId)
                        putString("farmer_price_per_meter", farmer.pricePerMeter.toString())
                        putString("farmer_timestamp", farmer.timestamp.toString())
                        putString("user_id", farmer.userId) // Sertakan userId
                    }

                    val editFragment = EditFarmersFragment()
                    editFragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit()
                }, { farmer ->
                    // Handle delete action
                    deleteFarmer(farmer)
                })

                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteFarmer(farmer: Farmer) {
        val key = farmer.key ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("lsm_pertanian").child(key)
        databaseRef.removeValue().addOnSuccessListener {
            Toast.makeText(context, "Data petani berhasil dihapus", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Gagal menghapus data petani", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showVerificationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Verifikasi Diperlukan")
            .setMessage("Hanya petani atau admin yang diizinkan menambah data.")
            .setPositiveButton("Verifikasi") { _, _ ->
                val intent = Intent(context, NavigationActivity::class.java)
                intent.putExtra("navigateTo", "FarmersFragment")
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showVerificationDialog(onVerified: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Verifikasi Diperlukan")
            .setMessage("Anda harus memverifikasi kalau Anda petani. Silakan konfirmasi untuk melanjutkan dan anda bisa menambah data.")
            .setPositiveButton("Verifikasi") { _, _ ->
                onVerified()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showInfoDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Informasi")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}

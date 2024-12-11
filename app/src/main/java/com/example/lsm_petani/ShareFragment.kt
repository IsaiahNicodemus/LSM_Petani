package com.example.lsm_petani

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ShareFragment : Fragment() {

    private lateinit var tvRole: TextView
    private lateinit var tvWelcome: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_share, container, false)
        tvRole = view.findViewById(R.id.tvRole)
        tvWelcome = view.findViewById(R.id.tvWelcome)
        fetchUserRole()
        return view
    }

    private fun fetchUserRole() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)
            userRef.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(String::class.java)
                val name = snapshot.child("username").getValue(String::class.java)
                if (role != null) {
                    tvRole.text = "Role Anda: $role"
                } else {
                    tvRole.text = "Role tidak ditemukan"
                }

                tvWelcome.text = if (name != null) "Halo, $name!" else "Halo, Pengguna!"
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
            }
        } else {
            tvRole.text = "Anda belum login"
        }
    }
}

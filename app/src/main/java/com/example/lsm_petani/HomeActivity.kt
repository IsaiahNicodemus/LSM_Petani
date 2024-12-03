package com.example.lsm_petani

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvRole = findViewById<TextView>(R.id.tvRole)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Ambil data role dari intent
        val role = intent.getStringExtra("role")
        tvRole.text = "Role: $role"

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Tombol Logout
        btnLogout.setOnClickListener {
            auth.signOut() // Logout pengguna dari Firebase
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Tutup HomeActivity
        }
    }
}

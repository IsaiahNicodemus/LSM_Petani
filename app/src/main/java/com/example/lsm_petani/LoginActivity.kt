package com.example.lsm_petani

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val CAMERA_PERMISSION_CODE = 100
    private val NOTIFICATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek apakah pengguna sudah login
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // Jika sudah login, arahkan ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        auth = FirebaseAuth.getInstance()

        // Periksa izin kamera dan notifikasi
        checkAndRequestCameraPermission()
        checkAndRequestNotificationPermission()

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString()
            val password = etPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val database = FirebaseDatabase.getInstance().getReference("users")
                            database.child(userId).get().addOnSuccessListener { snapshot ->
                                val role = snapshot.child("role").value.toString()

                                // Simpan status login
                                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                                // Pindah ke MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("role", role)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Periksa dan minta izin untuk akses kamera.
     */
    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            Toast.makeText(this, "Akses kamera sudah diberikan!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Periksa dan minta izin untuk menampilkan notifikasi.
     */
    private fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        } else {
            Toast.makeText(this, "Izin notifikasi sudah diberikan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Izin kamera berhasil diberikan!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Izin notifikasi berhasil diberikan!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

package com.example.lsm_petani

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referensi UI
        val etUsername = findViewById<EditText>(R.id.etUsername) // Username
        val etNewEmail = findViewById<EditText>(R.id.etNewEmail) // Email
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword) // Password
        val etNewPasswordConfirmation = findViewById<EditText>(R.id.etNewPasswordConfirmation) // Confirm Password
        val btnRegister = findViewById<Button>(R.id.btnRegister) // Button Register
        val loginRedirectText = findViewById<TextView>(R.id.loginRedirectText) // Redirect to Login

        auth = FirebaseAuth.getInstance()

        // Klik tombol register
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim() // Ambil input username
            val email = etNewEmail.text.toString().trim() // Ambil input email
            val password = etNewPassword.text.toString() // Ambil input password
            val confirmPassword = etNewPasswordConfirmation.text.toString().trim() // Konfirmasi password

            // Validasi input
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (password == confirmPassword) {
                    // Registrasi di Firebase Authentication
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    // Simpan data di Firebase Database
                                    val database = FirebaseDatabase.getInstance().getReference("users")
                                    val user = mapOf(
                                        "username" to username, // Simpan username
                                        "email" to email,
                                        "role" to "User" // Role otomatis User
                                    )
                                    database.child(userId).setValue(user)
                                        .addOnCompleteListener { dbTask ->
                                            if (dbTask.isSuccessful) {
                                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                                                // Simpan status login di SharedPreferences
                                                val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                                                // Pindah ke MainActivity
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Redirect ke halaman login
        loginRedirectText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

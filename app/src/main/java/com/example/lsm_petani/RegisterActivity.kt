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
        val etNewUsername = findViewById<EditText>(R.id.etNewUsername) // Email
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword) // Password
        val spinnerRole = findViewById<Spinner>(R.id.spinnerRole) // Role
        val btnRegister = findViewById<Button>(R.id.btnRegister) // Button Register

        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString() // Ambil input username
            val email = etNewUsername.text.toString() // Ambil input email
            val password = etNewPassword.text.toString() // Ambil input password
            val role = spinnerRole.selectedItem.toString() // Ambil input role

            // Validasi input
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
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
                                    "role" to role
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
                                            intent.putExtra("role", role)
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
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

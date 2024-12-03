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

        val etNewUsername = findViewById<EditText>(R.id.etNewUsername)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val spinnerRole = findViewById<Spinner>(R.id.spinnerRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            val email = etNewUsername.text.toString()
            val password = etNewPassword.text.toString()
            val role = spinnerRole.selectedItem.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val database = FirebaseDatabase.getInstance().getReference("users")
                                val user = mapOf(
                                    "username" to email,
                                    "role" to role
                                )
                                database.child(userId).setValue(user)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                                            // Pindah ke HomeActivity dan kirim data role
                                            val intent = Intent(this, HomeActivity::class.java)
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

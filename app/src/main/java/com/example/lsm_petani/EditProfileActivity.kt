package com.example.lsm_petani

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        val etUsername = findViewById<EditText>(R.id.etEditUsername)
        val etEmail = findViewById<EditText>(R.id.etEditEmail)
        val etPassword = findViewById<EditText>(R.id.etEditPassword)
        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)

        // Ambil data username dan email dari Firebase Database
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("users").child(userId)

            database.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    etUsername.setText(snapshot.child("username").value.toString())
                    etEmail.setText(snapshot.child("email").value.toString())
                } else {
                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Simpan perubahan ke Firebase
        btnSaveChanges.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (userId != null && username.isNotEmpty() && email.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("users").child(userId)
                val updates = mapOf(
                    "username" to username,
                    "email" to email
                )

                database.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.updateEmail(email)?.addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                if (password.isNotEmpty()) {
                                    user.updatePassword(password).addOnCompleteListener { passTask ->
                                        if (passTask.isSuccessful) {
                                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Failed to update password: ${passTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            } else {
                                Toast.makeText(this, "Failed to update email: ${emailTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

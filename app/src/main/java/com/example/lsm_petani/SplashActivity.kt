package com.example.lsm_petani

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // Pindah ke MainActivity jika sudah login
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Pindah ke LoginActivity jika belum login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish() // Hancurkan SplashActivity
    }
}

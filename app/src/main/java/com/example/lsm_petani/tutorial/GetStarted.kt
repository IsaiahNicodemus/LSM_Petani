package com.example.lsm_petani.tutorial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.lsm_petani.MainActivity
import com.example.lsm_petani.R

class GetStarted : AppCompatActivity() {

    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            val intent = Intent(this@GetStarted, MainActivity::class.java)
            intent.putExtra("navigateTo", "FarmersFragment") // Tambahkan informasi navigasi
            startActivity(intent)
            finish() // Tutup GetStarted Activity
        }
    }
}

package com.example.lsm_petani

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationView: NavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi komponen UI
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        // Set toggle untuk drawer navigation
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Load data pengguna ke header
        loadUserData()

        // Set fragment default (HomeFragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        // Tangkap data ekstra untuk navigasi
        val navigateTo = intent.getStringExtra("navigateTo")
        if (navigateTo == "FarmersFragment") {
            openFragment(FarmersFragment()) // Buka FarmersFragment
        } else {
            // Fragment default (misalnya HomeFragment)
            openFragment(HomeFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Ganti `fragment_container` dengan ID container Anda
            .commit()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Referensi ke data pengguna di Firebase
            val database = FirebaseDatabase.getInstance().getReference("users").child(userId)

            database.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").value?.toString() ?: "N/A"
                val email = snapshot.child("email").value?.toString() ?: "N/A"

                // Update header layout dengan username dan email
                val headerView = navigationView.getHeaderView(0)
                val tvUsername = headerView.findViewById<TextView>(R.id.tvUsername)
                val tvEmail = headerView.findViewById<TextView>(R.id.tvEmail)

                tvUsername.text = username
                tvEmail.text = email
            }.addOnFailureListener { error ->
                Toast.makeText(this, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_form -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FarmersFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()).commit()
            R.id.nav_share -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ShareFragment()).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutFragment()).commit()
            R.id.nav_logout -> handleLoginLogout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleLoginLogout() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // Logout logic
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            auth.signOut()
            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
            // Pindah ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Pindah ke LoginActivity jika belum login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        updateMenuTitle()
    }

    private fun updateMenuTitle() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val menuItem = navigationView.menu.findItem(R.id.nav_logout)
        menuItem?.title = if (isLoggedIn) "Logout" else "Login"
    }

    override fun onResume() {
        super.onResume()
        updateMenuTitle()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

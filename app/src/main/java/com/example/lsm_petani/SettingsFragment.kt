package com.example.lsm_petani

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SettingsFragment : Fragment() {

    private lateinit var tvUserRole: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "AppSettings"
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "default_notification_channel"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        tvUserRole = view.findViewById(R.id.tvUserRole)
        val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
        val btnThemeSettings = view.findViewById<Button>(R.id.btnThemeSettings)
        val btnNotifications = view.findViewById<Button>(R.id.btnNotifications)
        val btnHelpSupport = view.findViewById<Button>(R.id.btnHelpSupport)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("users")

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0)
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android 8+
        createNotificationChannel()

        // Fetch user role
        fetchUserRole()

        // Edit Profile
        btnEditProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Theme Settings
        btnThemeSettings.setOnClickListener {
            toggleTheme()
        }

        // Notifications
        btnNotifications.setOnClickListener {
            sendNotification()
        }

        // Help and Support
        btnHelpSupport.setOnClickListener {
            showHelpSupport()
        }

        return view
    }

    private fun fetchUserRole() {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail == "admin@example.com") {
            tvUserRole.text = "Role: Admin"
        } else {
            val currentUserId = auth.currentUser?.uid ?: return
            databaseRef.child(currentUserId).child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java)
                    tvUserRole.text = "Role: ${role ?: "User"}"
                }

                override fun onCancelled(error: DatabaseError) {
                    tvUserRole.text = "Role: Tidak Diketahui"
                }
            })
        }
    }

    private fun toggleTheme() {
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putBoolean("DarkMode", false).apply()
            Toast.makeText(requireContext(), "Tema berubah ke mode terang.", Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putBoolean("DarkMode", true).apply()
            Toast.makeText(requireContext(), "Tema berubah ke mode gelap.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleNotifications() {
        val isNotificationsEnabled = sharedPreferences.getBoolean("NotificationsEnabled", true)
        if (isNotificationsEnabled) {
            sharedPreferences.edit().putBoolean("NotificationsEnabled", false).apply()
            Toast.makeText(requireContext(), "Notifikasi mati.", Toast.LENGTH_SHORT).show()
        } else {
            sharedPreferences.edit().putBoolean("NotificationsEnabled", true).apply()
            sendNotification()
            Toast.makeText(requireContext(), "Notifikasi aktif.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Pengaturan Notifikasi",
                NotificationManager.IMPORTANCE_HIGH // Ubah menjadi HIGH agar muncul popup
            )
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun sendNotification() {
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Pastikan ikon ini ada di drawable
            .setContentTitle("Notifikasi Aktif")
            .setContentText("Pengaturan LSM Petani")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Gunakan PRIORITY_HIGH untuk popup
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Aktifkan semua efek (bunyi, popup, getaran)
            .setAutoCancel(true) // Notifikasi akan menghilang ketika ditekan oleh pengguna.

        notificationManager.notify(1, builder.build())

        Toast.makeText(requireContext(), "Notifikasi tampil langsung!", Toast.LENGTH_SHORT).show()
    }


    private fun showHelpSupport() {
        Toast.makeText(
            requireContext(),
            "Hubungi kami:\nEmail: support@example.com\nTelepon: +62 812-3456-7890",
            Toast.LENGTH_LONG
        ).show()
    }
}

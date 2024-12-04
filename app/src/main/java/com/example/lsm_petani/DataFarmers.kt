package com.example.lsm_petani

data class DataFarmers(
    val id: String = "", // Ensure this field exists and is used as the unique identifier
    val nama: String = "",
    val luasLahan: String = "",
    val namaPemilik: String = "",
    val noHandphone: String = "",
    val lokasi: String = "",
    val isVerified: Boolean = false
)

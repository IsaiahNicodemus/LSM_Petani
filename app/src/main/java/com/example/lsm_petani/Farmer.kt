package com.example.lsm_petani.model

data class Farmer(
    val key: String? = null, // Key Firebase
    val nama: String? = null,
    val lokasi: String? = null,
    val luasLahan: String? = null,
    val namaPemilik: String? = null,
    val noHandphone: String? = null,
    val photoUrl: String? = null,
    val status: Boolean = false // Field Baru
)



package com.example.lsm_petani.model

data class Farmer(
    val key: String? = null, // Key Firebase
    val nama: String? = null,
    val lokasi: String? = null,
    val luasLahan: String? = null,
    val namaPemilik: String? = null,
    val noHandphone: String? = null,
    val photoUrl: String? = null,
    val status: Boolean = false,
    val userId: String? = null, // ID pengguna yang menambahkan data
    val pricePerMeter: Double? = null, // Tambahkan harga per meter
    val timestamp: Long? = null // Tambahkan timestamp
)



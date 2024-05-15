package com.example.medicinedistributor
data class Order(
    val medicineId: String? = null,
    val distributorId: String? = null,
    val shopOwnerId: String? = null, // Add this field to store the shop owner ID
    val totalAmount: Int,
    val quantity: Int? = null,
    val status: String? = null
)


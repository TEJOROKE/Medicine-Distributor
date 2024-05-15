package com.example.medicinedistributor

data class OrderItem(
    val orderId: String,
    val shopOwnerId: String,
    val shopOwnerName: String, // Add shop owner's name
    val shopOwnerAddress: String, // Add shop owner's address
    val shopOwnerPhoneNumber: String, // Add shop owner's phone number
    val quantity: Int,
    val totalAmount: Double,
    val status: String,
    val imageUrl: String // Add this property for the image URL
)

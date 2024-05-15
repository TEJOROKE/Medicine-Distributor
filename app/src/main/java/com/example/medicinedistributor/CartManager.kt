package com.example.medicinedistributor

object CartManager {
    private val cartItems = ArrayList<BookedMedicine>()

    fun addToCart(bookedMedicine: BookedMedicine) {
        cartItems.add(bookedMedicine)
    }

    fun getCartItems(): List<BookedMedicine> {
        return cartItems
    }
}

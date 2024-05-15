package com.example.medicinedistributor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class mycart : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var userId: String
    private lateinit var cartItems: MutableList<CartItem>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mycart, container, false)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get current user ID
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        cartItems = mutableListOf()

        // Initialize RecyclerView adapter with an empty list
        adapter = CartAdapter(cartItems, requireContext())
        recyclerView.adapter = adapter

        // Fetch orders from Firebase
        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        val database = FirebaseDatabase.getInstance()
        val ordersRef = database.getReference("orders")

        // Query orders where shopOwnerId matches current user ID
        val query = ordersRef.orderByChild("shopOwnerId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (orderSnapshot in dataSnapshot.children) {
                    val medicineId = orderSnapshot.child("medicineId").getValue(String::class.java) ?: ""
                    val distributorId = orderSnapshot.child("distributorId").getValue(String::class.java) ?: ""
                    val quantity = orderSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    val totalPrice = orderSnapshot.child("totalAmount").getValue(Double::class.java) ?: 0.0
                    val status = orderSnapshot.child("status").getValue(String::class.java) ?: ""

                    // Fetch medicine details based on medicineId
                    val medicineRef = database.getReference("Distributor/$distributorId/medicines/$medicineId")
                    medicineRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(medicineSnapshot: DataSnapshot) {
                            val medicineName = medicineSnapshot.child("name").getValue(String::class.java) ?: ""
                            val medicineImageUrl = medicineSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                            val cartItem = CartItem(medicineName, quantity, totalPrice, status, medicineImageUrl)
                            cartItems.add(cartItem)
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}

data class CartItem(
    val medicineName: String,
    val quantity: Int,
    val totalPrice: Double,
    val status: String,
    val medicineImageUrl: String // Added property for medicine image URL
)

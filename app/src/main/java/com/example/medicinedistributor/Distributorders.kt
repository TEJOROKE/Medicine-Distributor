package com.example.medicinedistributor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Distributorders : Fragment(), OrderAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var userId: String
    private lateinit var orderItems: MutableList<OrderItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_distributorders, container, false)

        recyclerView = view.findViewById(R.id.recyclerviewdist)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        orderItems = mutableListOf()
        adapter = OrderAdapter(requireContext(), orderItems, this) // Pass the context here
        recyclerView.adapter = adapter

        fetchOrderItems()

        return view
    }

    private fun fetchOrderItems() {
        val database = FirebaseDatabase.getInstance()
        val ordersRef = database.getReference("orders")

        // Query orders where distributorId matches the current user ID
        val query = ordersRef.orderByChild("distributorId").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                orderItems.clear() // Clear the list before adding new items

                for (orderSnapshot in dataSnapshot.children) {
                    val orderId = orderSnapshot.key ?: ""
                    val shopOwnerId = orderSnapshot.child("shopOwnerId").getValue(String::class.java) ?: ""
                    val quantity = orderSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    val totalAmount = orderSnapshot.child("totalAmount").getValue(Double::class.java) ?: 0.0
                    val status = orderSnapshot.child("status").getValue(String::class.java) ?: ""

                    // Fetch additional shop owner details including image URL
                    val shopOwnerRef = database.getReference("ShopOwner/$shopOwnerId")
                    shopOwnerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(shopOwnerSnapshot: DataSnapshot) {
                            val shopOwnerName = shopOwnerSnapshot.child("shopName").getValue(String::class.java) ?: ""
                            val shopOwnerAddress = shopOwnerSnapshot.child("shopAddress").getValue(String::class.java) ?: ""
                            val shopOwnerPhoneNumber = shopOwnerSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""

                            // Fetch image URL from medicines collection under Distributor
                            val distributorId = orderSnapshot.child("distributorId").getValue(String::class.java) ?: ""
                            val medicineId = orderSnapshot.child("medicineId").getValue(String::class.java) ?: ""
                            val imageUrlRef = database.getReference("Distributor/$distributorId/medicines/$medicineId/imageUrl")
                            imageUrlRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(imageUrlSnapshot: DataSnapshot) {
                                    val imageUrl = imageUrlSnapshot.getValue(String::class.java) ?: ""

                                    // Add the order details to the list
                                    val orderItem = OrderItem(
                                        orderId,
                                        shopOwnerId,
                                        shopOwnerName,
                                        shopOwnerAddress,
                                        shopOwnerPhoneNumber,
                                        quantity,
                                        totalAmount,
                                        status,
                                        imageUrl
                                    )
                                    orderItems.add(orderItem)

                                    // Notify the adapter that the data set has changed
                                    adapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle error
                                }
                            })
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

    override fun onItemClick(orderItem: OrderItem) {
        // Handle item click, such as updating status, dispatch time, etc.
    }
}

package com.example.medicinedistributor

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class profilefrag : Fragment() {

    private lateinit var shopNameTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var phoneNumberTextView: TextView
    private lateinit var shopAddressTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var database: DatabaseReference
    private val TAG = "ProfileFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profilefrag, container, false)

        // Initialize views
        shopNameTextView = view.findViewById(R.id.shopname)
        userNameTextView = view.findViewById(R.id.username)
        phoneNumberTextView = view.findViewById(R.id.phonenumber)
        shopAddressTextView = view.findViewById(R.id.shopaddress)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        database = FirebaseDatabase.getInstance().reference

        // Fetch user details based on user type (Distributorr or ShopOwner)
        fetchUserDetails(currentUser.uid)

        return view
    }

    private fun fetchUserDetails(userId: String) {
        database.child("Distributor").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val distributor = dataSnapshot.getValue(Distributorr::class.java)
                    distributor?.let {
                        displayDistributorDetails(it)
                    }
                } else {
                    // If user is not found under Distributorr, try fetching from ShopOwner
                    fetchShopOwnerDetails(userId)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching distributor details: ${databaseError.message}", databaseError.toException())
            }
        })
    }

    private fun fetchShopOwnerDetails(userId: String) {
        database.child("ShopOwner").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val shopOwner = dataSnapshot.getValue(ShopOwner::class.java)
                    shopOwner?.let {
                        displayShopOwnerDetails(it)
                    }
                } else {
                    Log.e(TAG, "User not found for ID: $userId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching shop owner details: ${databaseError.message}", databaseError.toException())
            }
        })
    }

    private fun displayDistributorDetails(distributor: Distributorr) {
        shopNameTextView.text = distributor.shopName
        userNameTextView.text = distributor.userName
        phoneNumberTextView.text = distributor.phoneNumber
        shopAddressTextView.text = distributor.shopAddress
    }

    private fun displayShopOwnerDetails(shopOwner: ShopOwner) {
        shopNameTextView.text = shopOwner.shopName
        userNameTextView.text = shopOwner.userName
        phoneNumberTextView.text = shopOwner.phoneNumber
        shopAddressTextView.text = shopOwner.shopAddress
    }
}


// Distributorr data class
data class Distributorr(
    val phoneNumber: String? = "",
    val shopAddress: String? = "",
    val shopName: String? = "",
    val userName: String? = "",
    val userType: String? = ""
)

// ShopOwner data class
data class ShopOwner(
    val phoneNumber: String? = "",
    val shopAddress: String? = "",
    val shopName: String? = "",
    val userName: String? = "",
    val userType: String? = ""
)

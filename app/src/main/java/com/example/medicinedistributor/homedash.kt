package com.example.medicinedistributor

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*

class homedash() : Fragment(), AdapterHome.OnItemClickListener,Parcelable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterHome
    private lateinit var database: FirebaseDatabase
    private lateinit var medicineList: MutableList<Medicine>
    private lateinit var auth: FirebaseAuth
    private var shopOwnerRef: DatabaseReference? = null
    private var distributorRef: DatabaseReference? = null


    private lateinit var searchView: SearchView
    private lateinit var originalMedicineList: MutableList<Medicine>

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_homedash, container, false)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        searchView = view.findViewById(R.id.searchView)

        database = FirebaseDatabase.getInstance()
        medicineList = mutableListOf()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            checkUserType(userId)
        }

       // originalMedicineList = mutableListOf()

        // Set up search functionality
        setUpSearch()


        adapter = AdapterHome(requireContext(), medicineList, this)
        recyclerView.adapter = adapter


        return view
    }

    private fun setUpSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { query ->
                    Log.d("homedash", "Search query: $query")
                    adapter.filter(query)
                }
                return true
            }
        })
    }
    private fun filterMedicines(query: String) {
        try {
            Log.d("homedash", "Filtering medicines with query: $query")
            medicineList.clear()
            val filteredList = originalMedicineList.filter { medicine ->
                val nameMatch = medicine.name?.contains(query, ignoreCase = true) ?: false
                nameMatch
            }
            Log.d("homedash", "Filtered list size: ${filteredList.size}")
            medicineList.addAll(filteredList)
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            showToast("Error filtering medicines: ${e.message}")
            e.printStackTrace()
        }
    }



    private fun checkUserType(userId: String) {
        shopOwnerRef = database.getReference("ShopOwner")
        distributorRef = database.getReference("Distributor")

        shopOwnerRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(userId)) {
                    // User is a shop owner
                    fetchAllMedicinesFromAllDistributors()
                } else {
                    distributorRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(userId)) {
                                // User is a distributor
                                fetchDistributorMedicines(userId)
                            } else {
                                // User is neither a shop owner nor a distributor
                                // Handle this case as needed
                                showToast("User is neither a shop owner nor a distributor")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                            showToast("Failed to retrieve data: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                showToast("Failed to retrieve data: ${error.message}")
            }
        })
    }

    private fun fetchAllMedicinesFromAllDistributors() {
        val distributorsRef = database.getReference("Distributor")

        distributorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    medicineList.clear()
                    for (distributorSnapshot in snapshot.children) {
                        val distributorId = distributorSnapshot.key
                        distributorId?.let { fetchMedicinesFromDistributor(it) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                showToast("Failed to retrieve distributors: ${error.message}")
            }
        })
    }

    private fun fetchMedicinesFromDistributor(distributorId: String) {
        val reference = database.getReference("Distributor").child(distributorId).child("medicines")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val medicine = dataSnapshot.getValue(Medicine::class.java)
                        medicine?.let {
                            medicineList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                showToast("Failed to retrieve medicines for distributor $distributorId: ${error.message}")
            }
        })
    }


    private fun fetchDistributorMedicines(distributorId: String) {
        val reference = database.getReference("Distributor").child(distributorId).child("medicines")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    medicineList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val medicine = dataSnapshot.getValue(Medicine::class.java)
                        medicine?.let {
                            medicineList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                showToast("Failed to retrieve distributor medicines: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(medicine: Medicine) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            checkUserType(userId) { isDistributor ->
                val intent = if (isDistributor) {
                    Intent(requireContext(), ModifyMedicineActivity::class.java)
                } else {
                    Intent(requireContext(), productdesc_activity::class.java)
                }

                // Add distributor ID and selected medicine as extras
                val distributorId = if (isDistributor) userId else medicine.distributorId
                intent.putExtra("medicine", medicine)
                intent.putExtra("distributorId", distributorId)
                startActivity(intent)
            }
        }
    }


    private fun checkUserType(userId: String, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val shopOwnerRef = database.getReference("ShopOwner")
        val distributorRef = database.getReference("Distributor")

        shopOwnerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(userId)) {
                    onComplete(false) // User is a shop owner
                } else {
                    distributorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(userId)) {
                                onComplete(true) // User is a distributor
                            } else {
                                onComplete(false) // User is neither a shop owner nor a distributor
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<homedash> {
        override fun createFromParcel(parcel: Parcel): homedash {
            return homedash(parcel)
        }

        override fun newArray(size: Int): Array<homedash?> {
            return arrayOfNulls(size)
        }
    }

}
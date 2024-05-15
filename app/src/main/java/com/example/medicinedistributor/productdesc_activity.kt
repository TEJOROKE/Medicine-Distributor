package com.example.medicinedistributor

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class productdesc_activity : AppCompatActivity() {

    private lateinit var medNameTextView: TextView
    private lateinit var brandNameTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var availableQuantityTextView: TextView
    private lateinit var distributorNameTextView: TextView
    private lateinit var distributorContactTextView: TextView
    private lateinit var quantityInputLayout: TextInputLayout
    private lateinit var medicinedescription :TextView
    private lateinit var bookButton: Button
    private lateinit var medicine: Medicine
    private lateinit var totalAmountTextView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productdesc_activity)

        // Initialize views
        medNameTextView = findViewById(R.id.medname)
        brandNameTextView = findViewById(R.id.brandname)
        priceTextView = findViewById(R.id.price)
        availableQuantityTextView = findViewById(R.id.availableQuantity)
        distributorNameTextView = findViewById(R.id.distributorName)
        distributorContactTextView = findViewById(R.id.distributorContact)
        quantityInputLayout = findViewById(R.id.textInputLayout)
        bookButton = findViewById(R.id.bookButton)
        totalAmountTextView=findViewById(R.id.totalAmountTextView)
        medicinedescription=findViewById(R.id.medicineDescription)

        // Retrieve Medicine object from intent
        medicine = intent.getParcelableExtra("medicine") ?: Medicine()

        // Display Medicine details
        medNameTextView.text = medicine.name
        brandNameTextView.text = medicine.manufacturer
        priceTextView.text = "Price: Rs. ${medicine.price}"
        availableQuantityTextView.text = "Available Quantity: ${medicine.quantity}"
        medicinedescription.text=medicine.description

        // Load Medicine image
        Glide.with(this)
            .load(medicine.imageUrl) // Load image URL from Medicine object
            .placeholder(R.drawable.baseline_insert_photo_24) // Placeholder image while loading
            .error(R.drawable.baseline_home_24) // Error image if loading fails
            .into(findViewById(R.id.medicineImage))

        // Fetch distributor ID for the medicine
        fetchDistributorIdForMedicine(medicine.id ?: "")

        fetchDistributorDetails(medicine.distributorId?:"")


        // Set a text watcher to the quantity input field

        quantityInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                // Retrieve quantity input from TextInputEditText
                val quantity = s.toString().toIntOrNull()

                // Calculate total amount
// Calculate total amount
                val totalAmount = quantity?.let { qty ->
                    medicine.price?.let { price ->
                        qty * price
                    } ?: 0
                } ?: 0

                // Set the total amount text
                totalAmountTextView.text = "Total Amount: Rs. $totalAmount"
            }
        })





        // Set onClickListener for Book Button
        bookButton.setOnClickListener {
            // Retrieve quantity input from TextInputEditText
            val quantity = quantityInputLayout.editText?.text.toString().toIntOrNull()

            if (quantity != null && quantity > 0 && quantity <= medicine.quantity ?: 0) {
                // Proceed to book the medicine
                bookMedicine(quantity)
            } else {
                // Show error message if quantity is invalid
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDistributorIdForMedicine(medicineId: String) {
        val database = FirebaseDatabase.getInstance()
        val medicinesRef = database.getReference("medicines")
        val query = medicinesRef.orderByChild("id").equalTo(medicineId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (medSnapshot in dataSnapshot.children) {
                        val distributorId =
                            medSnapshot.child("distributorId").value.toString() // Retrieve distributorId from medicine
                        fetchDistributorDetails(distributorId)
                    }
                } else {
                    // Handle case where medicine ID is not found
                    Log.e(TAG, "Medicine with ID $medicineId not found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.e(TAG, "Error fetching medicine details", databaseError.toException())
            }
        })
    }

    private fun fetchDistributorDetails(distributorId: String) {
        val database = FirebaseDatabase.getInstance()
        val distributorRef = database.getReference("Distributor").child(distributorId)

        distributorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val distributor = dataSnapshot.getValue(Distributor::class.java)
                    distributor?.let {
                        distributorNameTextView.text = "Distributor Name: ${it.shopName}"
                        distributorContactTextView.text =
                            "Contact: ${it.phoneNumber}\nAddress: ${it.shopAddress}"
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error fetching distributor details", databaseError.toException())
            }
        })
    }

    private fun bookMedicine(quantity: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance()

        currentUser?.let { user ->
            val userId = user.uid
            val orderRef = database.getReference("orders").push()

            // Calculate total amount
            val totalAmount = quantity * (medicine.price ?: 0).toInt()

            // Create an Order object
            val order = Order(
                medicineId = medicine.id ?: "",
                distributorId = medicine.distributorId ?: "", // Using medicine's distributorId
                shopOwnerId = userId,
                totalAmount = totalAmount,
                quantity = quantity,
                status = "pending"
            )

            // Save the order to the database
            orderRef.setValue(order)
                .addOnSuccessListener {
                    // Show success message if the order is successfully placed
                    Toast.makeText(
                        this@productdesc_activity,
                        "Medicine booked successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Update available quantity in the medicines collection
                    updateAvailableQuantity(medicine.id ?: "", quantity)
                }
                .addOnFailureListener {
                    // Show error message if booking fails
                    Toast.makeText(
                        this@productdesc_activity,
                        "Failed to book medicine",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            // Handle case where currentUser is null
            Toast.makeText(this@productdesc_activity, "User not logged in", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateAvailableQuantity(medicineId: String, bookedQuantity: Int) {
        val database = FirebaseDatabase.getInstance()
        val medicinesRef = database.getReference("Distributor/${medicine.distributorId}/medicines").child(medicineId)

        // Retrieve current available quantity from database
        medicinesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentAvailableQuantity = dataSnapshot.child("quantity").getValue(Int::class.java) ?: 0

                // Calculate new available quantity
                val newAvailableQuantity = currentAvailableQuantity - bookedQuantity

                // Update available quantity in the database
                dataSnapshot.ref.child("quantity").setValue(newAvailableQuantity)
                    .addOnSuccessListener {
                        Log.d(TAG, "Available quantity updated successfully")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to update available quantity", it)
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error updating available quantity", databaseError.toException())
            }
        })
    }

}
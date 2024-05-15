package com.example.medicinedistributor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ModifyMedicineActivity : AppCompatActivity() {

    private lateinit var medicine: Medicine
    private lateinit var editTextQuantity: EditText
    private lateinit var editTextPrice: EditText

    private lateinit var database: FirebaseDatabase
    private lateinit var medicineRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_medicine)

        // Get medicine details from intent
        medicine = intent.getParcelableExtra("medicine")!!

        // Initialize views
        val textViewName: TextView = findViewById(R.id.textViewName)
        val textViewDescription: TextView = findViewById(R.id.textViewDescription)
        val textViewManufacturer: TextView = findViewById(R.id.textViewManufacturer)
        val textViewQuantity: TextView = findViewById(R.id.textViewQuantity)
        val textViewPrice: TextView = findViewById(R.id.textViewPrice)
        editTextQuantity = findViewById(R.id.editTextQuantity)
        editTextPrice = findViewById(R.id.editTextPrice)
        val buttonUpdate: Button = findViewById(R.id.buttonUpdate)
        val buttonDelete: Button = findViewById(R.id.buttonDelete)
        val imageViewMedicine: ImageView = findViewById(R.id.imageViewMedicine)

        // Set medicine details to views
        textViewName.text = medicine.name
        textViewDescription.text = medicine.description
        textViewManufacturer.text = medicine.manufacturer
        textViewQuantity.text = medicine.quantity.toString()
        textViewPrice.text = medicine.price.toString()

        // Load medicine image using Glide library
        Glide.with(this)
            .load(medicine.imageUrl)
            .placeholder(R.drawable.baseline_insert_photo_24) // Placeholder image while loading
            .error(R.drawable.baseline_home_24) // Error image if loading fails
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized image
            .into(imageViewMedicine)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        medicineRef = database.getReference("Distributor/${FirebaseAuth.getInstance().currentUser!!.uid}/medicines/${medicine.id}")

        // Update button click listener
        buttonUpdate.setOnClickListener {
            val newQuantity = editTextQuantity.text.toString().toIntOrNull()
            val newPrice = editTextPrice.text.toString().toDoubleOrNull()

            if (newQuantity == null || newPrice == null) {
                showToast("Invalid quantity or price")
            } else {
                updateMedicine(newQuantity, newPrice)
            }
        }

        // Delete button click listener
        buttonDelete.setOnClickListener {
            deleteMedicine()
        }
    }

    private fun updateMedicine(newQuantity: Int, newPrice: Double) {
        medicineRef.child("quantity").setValue(newQuantity)
        medicineRef.child("price").setValue(newPrice)
        showToast("Medicine updated successfully")
    }

    private fun deleteMedicine() {
        // Get the original medicine ID
        val originalMedicineId = medicine.id

        // Construct the reference using the original medicine ID
        val originalMedicineRef = database.getReference("Distributor/${FirebaseAuth.getInstance().currentUser!!.uid}/medicines/$originalMedicineId")

        // Delete the medicine using the original reference
        originalMedicineRef.removeValue()
            .addOnSuccessListener {
                showToast("Medicine deleted successfully")
                finish() // Close the activity after deletion
            }
            .addOnFailureListener { exception ->
                showToast("Failed to delete medicine: ${exception.message}")
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

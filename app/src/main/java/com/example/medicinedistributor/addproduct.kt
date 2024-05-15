package com.example.medicinedistributor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.medicinedistributor.Medicine
import com.example.medicinedistributor.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class addproduct : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var addButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var selectedImageView: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var userId: String // Define userId property

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_addproduct, container, false)

        addButton = view.findViewById(R.id.Buttonadd)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        selectedImageView = view.findViewById(R.id.selectedImageView)

        // Retrieve userId when the fragment is created
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        addButton.setOnClickListener {
            addProductToDatabase()
        }

        selectImageButton.setOnClickListener {
            openImageChooser()
        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        return view
    }

    private fun addProductToDatabase() {
        // Retrieve product details from the input fields
        val medicineName = view?.findViewById<TextInputEditText>(R.id.medicineNameEditText)?.text.toString()
        val price = view?.findViewById<TextInputEditText>(R.id.priceEditText)?.text.toString().toDouble()
        val manufacturer = view?.findViewById<TextInputEditText>(R.id.manufacturerEditText)?.text.toString()
        val quantity = view?.findViewById<TextInputEditText>(R.id.quantityEditText)?.text.toString().toInt()
        val description = view?.findViewById<TextInputEditText>(R.id.descriptionEditText)?.text.toString()

        // Ensure the user is logged in before adding the product
        if (userId.isNotEmpty()) {
            val medicineRef = database.getReference("Distributor").child(userId).child("medicines").push()
            val medicineId = medicineRef.key

            val medicine = Medicine(medicineId, medicineName, price, manufacturer, quantity, description)

            medicine.distributorId=userId

            // Push the medicine to the database
            medicineRef.setValue(medicine)
                .addOnSuccessListener {
                    uploadImageToStorage(medicineId)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add medicine: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            selectedImageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToStorage(medicineId: String?) {
        if (selectedImageUri != null && medicineId != null) {
            val storageRef = storage.getReference("medicine_images/$medicineId.jpg")

            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { uploadTask ->
                    // Get the download URL for the uploaded image
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Save the image URL along with other medicine details in the Realtime Database
                        val imageUrl = uri.toString()
                        val medicineRef = database.getReference("Distributor").child(userId).child("medicines").child(medicineId)
                        medicineRef.child("imageUrl").setValue(imageUrl)
                    }
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}

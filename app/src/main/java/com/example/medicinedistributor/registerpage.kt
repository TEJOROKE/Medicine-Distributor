package com.example.medicinedistributor

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class registerpage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var shopNameEditText: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var shopAddressEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var textviewdeter : TextView
    private lateinit var textviewaddr : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerpage)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        shopNameEditText = findViewById(R.id.shopname)
        userNameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)
        phoneNumberEditText = findViewById(R.id.phonenumber)
        shopAddressEditText = findViewById(R.id.shop_address)
        registerButton = findViewById(R.id.registerbtn)
        radioGroup = findViewById(R.id.radioGroup)
        textviewdeter=findViewById(R.id.textviewdeter)
        textviewaddr=findViewById(R.id.textviewaddr)


        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton -> {
                    // Distributor radio button selected
                    textviewdeter.visibility = View.VISIBLE
                    textviewdeter.text="Enter Distributor Name"
                    shopAddressEditText.visibility=View.VISIBLE
                    shopNameEditText.visibility=View.VISIBLE
                    textviewaddr.visibility=View.VISIBLE
                    textviewaddr.text="Enter Distributor Address"// Show the TextView

                    // Show the TextView
                }
                R.id.radioButton2 -> {
                    // ShopOwner radio button selected
                    textviewdeter.visibility = View.VISIBLE
                    shopAddressEditText.visibility=View.VISIBLE
                    shopNameEditText.visibility=View.VISIBLE

                    textviewdeter.text="Enter Shop Name"
                    textviewaddr.visibility=View.VISIBLE
                    textviewaddr.text="Enter Shop Address"
                    textviewaddr.hint="Enter shop Address"
                    // Show the TextView
                }
            }
        }

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val shopName = shopNameEditText.text.toString().trim()
        val userName = userNameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val shopAddress = shopAddressEditText.text.toString().trim()

        if (shopName.isEmpty() || userName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty() || shopAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userName).matches()) {
            userNameEditText.error = "Enter a valid email address"
            userNameEditText.requestFocus()
            return
        }

        if (password.length !in 8..14) {
            passwordEditText.error = "Password must be 8-14 characters long"
            passwordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            confirmPasswordEditText.requestFocus()
            return
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(userName, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    val userType = when (radioGroup.checkedRadioButtonId) {
                        R.id.radioButton -> "Distributor"
                        R.id.radioButton2 -> "ShopOwner"
                        else -> ""
                    }

                    // Data to be stored in Firestore
                    val userData = hashMapOf(
                        "shopName" to shopName,
                        "userName" to userName,
                        "phoneNumber" to phoneNumber,
                        "shopAddress" to shopAddress,
                        "userType" to userType
                    )

                    // Store user data in Firestore
                    userId?.let {
                        db.collection("users").document(it).set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }

                    // Store user data in Realtime Database based on userType
                    userId?.let {
                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.getReference(userType).child(it)
                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                // Data stored successfully
                                Toast.makeText(this, "User data stored successfully in Realtime Database", Toast.LENGTH_SHORT).show()
                                // Redirect to MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Handle registration failure
                    Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

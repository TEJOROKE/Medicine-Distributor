package com.example.medicinedistributor

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var loginButton: Button
    private lateinit var textRegister: TextView
    private lateinit var username: EditText
    private lateinit var password: EditText

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loginButton = findViewById(R.id.registerbtn)
        textRegister = findViewById(R.id.registertv)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        // Set onTouchListener for the password EditText to toggle password visibility
        password.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP &&
                event.rawX >= (password.right - password.compoundDrawables[2].bounds.width())
            ) {
                // Calculate the position of the touch event relative to the right drawable
                togglePasswordVisibility()
                return@setOnTouchListener true
            }
            false
        }

        loginButton.setOnClickListener {
            val email = username.text.toString().trim()
            val pass = password.text.toString().trim()

            if (email.isEmpty()) {
                username.error = "Email is required"
                username.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                username.error = "Enter a valid email address"
                username.requestFocus()
                return@setOnClickListener
            }

            if (pass.isEmpty()) {
                password.error = "Password is required"
                password.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, pass)
        }

        textRegister.setOnClickListener {
            val intent = Intent(this, registerpage::class.java)
            startActivity(intent)
        }
    }

    private fun togglePasswordVisibility() {
        // Toggle password visibility when eye icon is clicked
        val selection = password.selectionEnd
        if (password.transformationMethod == null) {
            // Password is currently visible, so hide it
            password.transformationMethod =
                android.text.method.PasswordTransformationMethod.getInstance()
        } else {
            // Password is currently hidden, so show it
            password.transformationMethod = null
        }
        password.setSelection(selection)
    }

    // Other methods (loginUser, isUserLoggedIn, getCurrentUserId, saveSession, navigateToHomePage) remain unchanged


    override fun onStart() {
        super.onStart()
        // Check if the user is already logged in
        if (isUserLoggedIn()) {
            // If user is logged in, navigate to appropriate screen
            val userId = getCurrentUserId()
            navigateToHomePage(userId)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        saveSession(userId)
                        navigateToHomePage(userId)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun isUserLoggedIn(): Boolean {
        // Check if the session data exists
        val sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE)
        return sharedPreferences.contains("userId")
    }

    private fun getCurrentUserId(): String {
        // Retrieve the user ID from the session data
        val sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE)
        return sharedPreferences.getString("userId", "") ?: ""
    }

    private fun saveSession(userId: String) {
        // Save the user ID to the session data
        val sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.apply()
    }

    // modifymeddis.kt
    private fun navigateToHomePage(userId: String) {
        val distributorRef = database.getReference("Distributor").child(userId)
        val shopOwnerRef = database.getReference("ShopOwner").child(userId)

        distributorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User is a distributor
                    val intent = Intent(this@MainActivity, mainhomepage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // User is not a distributor, check if user is a shop owner
                    shopOwnerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // User is a shop owner
                                val intent =
                                    Intent(this@MainActivity, userhome_activity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // User type not found, navigate to default home page

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error while fetching shop owner data
                            Log.e(TAG, "Error fetching shop owner data", error.toException())
                            val intent = Intent(this@MainActivity, mainhomepage::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error while fetching distributor data
                Log.e(TAG, "Error fetching distributor data", error.toException())

                finish()
            }
        })
    }
}
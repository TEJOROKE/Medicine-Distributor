package com.example.medicinedistributor


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.medicinedistributor.Distributorders
import com.example.medicinedistributor.MainActivity
import com.example.medicinedistributor.R
import com.example.medicinedistributor.aboutapp
import com.example.medicinedistributor.addproduct
import com.example.medicinedistributor.homedash
import com.example.medicinedistributor.mycart
import com.example.medicinedistributor.profilefrag
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class mainhomepage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainhomepage)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // Ignore red line errors
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,homedash()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container,homedash()).commit()
            R.id.nav_profile->supportFragmentManager.beginTransaction().replace(R.id.fragment_container,profilefrag()).commit()
            R.id.myorder -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Distributorders()).commit()
            R.id.addproduct->supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                addproduct()
            ).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, aboutapp()).commit()
            R.id.nav_logout -> logoutUser()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun logoutUser() {
        // Clear session data
        clearSession()

        // Navigate to the login screen
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun clearSession() {
        // Clear session data (userId in this case)
        val sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("userId")
        editor.apply()
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

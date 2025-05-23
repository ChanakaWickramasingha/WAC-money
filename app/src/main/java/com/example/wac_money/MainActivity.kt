package com.example.wac_money

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "setContentView completed")

            // Set up the toolbar
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            Log.d(TAG, "Toolbar setup completed")

            // Get NavHostFragment and NavController
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            Log.d(TAG, "NavController initialized")

            val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

            // Setup the bottom navigation with the nav controller
            navView.setupWithNavController(navController)
            Log.d(TAG, "BottomNavigationView setup with NavController completed")

            // Setup the ActionBar with the nav controller
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_dashboard,
                    R.id.navigation_transactions,
                    R.id.navigation_budget,
                    R.id.navigation_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            Log.d(TAG, "ActionBar setup with NavController completed")

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

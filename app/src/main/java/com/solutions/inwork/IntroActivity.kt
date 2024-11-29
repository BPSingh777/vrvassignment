package com.solutions.inwork

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat

class IntroActivity : AppCompatActivity() {


    private lateinit var acceptbtn : Button
    private lateinit var denybtn : Button

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        acceptbtn = findViewById(R.id.acceptbutton)
        denybtn = findViewById(R.id.denybtn)

        acceptbtn.setOnClickListener {

            requestBackgroundPermission()

        }

        denybtn.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }




    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPermission(){


        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
                // Manifest.permission.READ_EXTERNAL_STORAGE,
                // Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            123
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Background Location Access")
                    .setMessage("This app collects background location data to enable geofencing for automatic CheckIn/Out even when the app is closed or not in use. Hence ALLOW ALL THE TIME to use this feature.")
                    .setPositiveButton("Continue") { dialog, _ ->
                        // Request location permission here
                        requestBackgroundLocationPermission()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // Handle cancellation or show an alternative flow
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .create()

                dialog.show()
            } else {
                // Foreground location permission is denied, handle the scenario
                // ...
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Background location permission is granted, proceed with your logic
                // ...
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()
                // Background location permission is denied, handle the scenario
                // ...
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Background location permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101)
        }
    }

}
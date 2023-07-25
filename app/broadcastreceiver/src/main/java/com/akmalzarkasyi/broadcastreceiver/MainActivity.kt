package com.akmalzarkasyi.broadcastreceiver

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.akmalzarkasyi.broadcastreceiver.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPermission.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.RECEIVE_SMS),
                    SMS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_REQUEST_CODE) {
            when (PackageManager.PERMISSION_GRANTED) {
                grantResults[0] -> Toast.makeText(
                    this,
                    "Sms receiver permission diterima",
                    Toast.LENGTH_SHORT
                ).show()

                else -> Toast.makeText(this, "Sms receiver permission ditolak", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        private const val SMS_REQUEST_CODE = 101
    }
}
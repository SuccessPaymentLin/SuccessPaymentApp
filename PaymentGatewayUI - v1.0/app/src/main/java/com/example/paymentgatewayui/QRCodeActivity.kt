package com.example.paymentgatewayui

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class QRCodeActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var verificationLink: String? = null
    private var pollingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        imageView = findViewById(R.id.qrImageView)
        verificationLink = intent.getStringExtra("verificationLink")
        val qrCodeBase64 = intent.getStringExtra("qrCodeBase64")

        if (!qrCodeBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
                startPollingForResult()
            } catch (e: Exception) {
                Toast.makeText(this, "Error decoding QR code", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "No QR code provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startPollingForResult() {
        if (verificationLink.isNullOrEmpty()) {
            Toast.makeText(this, "Verification link is missing", Toast.LENGTH_SHORT).show()
            return
        }

        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val connection = URL(verificationLink).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)
                        val status = json.optString("status").lowercase()

                        Log.d("Polling", "Received status: $status")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@QRCodeActivity, "Status: $status", Toast.LENGTH_SHORT).show()
                            when (status) {
                                "approved" -> {
                                    Log.d("Polling", "Status is approved. Navigating to success.")
                                    goToSuccessPage()
                                    cancel() // stop polling
                                }
                                "declined" -> {
                                    Log.d("Polling", "Status is declined. Navigating to failure.")
                                    goToFailurePage()
                                    cancel() // stop polling
                                }
                                else -> {
                                    Log.d("Polling", "Waiting for final status...")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Polling", "Error: ${e.message}")
                    // Optional: Show an error Toast here, if needed
                }

                delay(3000) // Poll every 3 seconds
            }
        }
    }

    private fun goToSuccessPage() {
        startActivity(Intent(this, SuccessActivity::class.java))
        finish()
    }

    private fun goToFailurePage() {
        startActivity(Intent(this, FailureActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
    }
}
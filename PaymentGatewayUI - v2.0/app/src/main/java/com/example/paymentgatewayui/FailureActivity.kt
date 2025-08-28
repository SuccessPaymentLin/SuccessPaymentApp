package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FailureActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var redirectRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failure)

        // Get error message passed from PaymentMethodActivity
        val errorMessage = intent.getStringExtra("errorMessage") ?: "An unexpected error occurred"

        // Set error message to TextView
        val errorTextView: TextView = findViewById(R.id.failureMessageText)
        errorTextView.text = errorMessage

        val retryButton: Button = findViewById(R.id.tryAgainButton)
        retryButton.setOnClickListener {
            cancelRedirect()
            navigateBackToTransactionType()
        }

        // Auto-redirect after 5 seconds
        redirectRunnable = Runnable {
            navigateBackToTransactionType()
        }
        handler.postDelayed(redirectRunnable!!, 5000)
    }

    private fun navigateBackToTransactionType() {
        val intent = Intent(this, TransactionTypeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun cancelRedirect() {
        redirectRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRedirect()
    }
}
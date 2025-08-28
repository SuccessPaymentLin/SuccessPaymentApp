package com.example.paymentgatewayui

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private var selectedTransactionType: String? = null // Stores "preauth", "auth", or "refund"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContentView(R.layout.activity_main)

        val amountField = findViewById<EditText>(R.id.amountInput)
        val nextButton = findViewById<Button>(R.id.nextButton)

        val btnPreAuth = findViewById<Button>(R.id.btnPreAuth)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        val btnRefund = findViewById<Button>(R.id.btnRefund)

        btnPreAuth.setOnClickListener {
            selectedTransactionType = "preauth"
            Toast.makeText(this, "Pre-Authorization selected", Toast.LENGTH_SHORT).show()
        }

        btnAuth.setOnClickListener {
            selectedTransactionType = "auth"
            Toast.makeText(this, "Authorization selected", Toast.LENGTH_SHORT).show()
        }

        btnRefund.setOnClickListener {
            showPasswordDialog {
                selectedTransactionType = "refund"
                Toast.makeText(this, "Refund selected", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            val amountText = amountField.text.toString()
            val amount = amountText.toDoubleOrNull()

            if (selectedTransactionType == null) {
                Toast.makeText(this, "Please select a transaction type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed to next screen
            val intent = Intent(this, AmountConfirmationActivity::class.java)
            intent.putExtra("amount", amount.toString())
            intent.putExtra("transactionType", selectedTransactionType)
            startActivity(intent)
        }
    }

    private fun showPasswordDialog(onSuccess: () -> Unit) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Enter Refund Password")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val password = input.text.toString()
                if (verifyPassword(password)) {
                    onSuccess()
                } else {
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun verifyPassword(password: String): Boolean {
        // Replace with your actual logic or secure password check
        return password == "admin123"
    }
}
package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class TransactionTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TransactionTypeActivity", "onCreate called")

        setContentView(R.layout.activity_transaction_type)

        val btnPreAuth = findViewById<Button>(R.id.btnPreAuth)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        val btnRefund = findViewById<Button>(R.id.btnRefund)

        btnPreAuth.setOnClickListener {
            navigateToAmountInput("preauth")
        }

        btnAuth.setOnClickListener {
            navigateToAmountInput("auth")
        }

        btnRefund.setOnClickListener {
            showPasswordDialog {
                navigateToAmountInput("refund")
            }
        }
    }

    private fun navigateToAmountInput(transactionType: String) {
        val intent = Intent(this, AmountInputActivity::class.java)
        intent.putExtra("transactionType", transactionType)
        startActivity(intent)
    }

    private fun showPasswordDialog(onSuccess: () -> Unit) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

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
        return password == "admin123" // Adjust as needed
    }
}

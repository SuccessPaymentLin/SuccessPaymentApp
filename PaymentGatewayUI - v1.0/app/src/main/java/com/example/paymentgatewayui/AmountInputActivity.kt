package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AmountInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amount_input)

        val transactionType = intent.getStringExtra("transactionType")

        val amountInput = findViewById<EditText>(R.id.editAmount)
        val nextButton = findViewById<Button>(R.id.btnNextAmount)

        nextButton.setOnClickListener {
            val transactionType = intent.getStringExtra("transactionType")
            val amount = amountInput.text.toString()

            val intent = Intent(this, PaymentMethodActivity::class.java)
            intent.putExtra("transactionType", transactionType)
            intent.putExtra("amount", amount)
            startActivity(intent)
        }
    }
}
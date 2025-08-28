package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TransactionTypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_type)

        findViewById<Button>(R.id.btnPreAuth).setOnClickListener {
            goToAmountScreen("PRE_AUTH")
        }

        findViewById<Button>(R.id.btnAuth).setOnClickListener {
            goToAmountScreen("AUTH")
        }

        findViewById<Button>(R.id.btnRefund).setOnClickListener {
            goToAmountScreen("REFUND")
        }
    }

    private fun goToAmountScreen(type: String) {
        val intent = Intent(this, AmountInputActivity::class.java)
        intent.putExtra("transactionType", type)
        startActivity(intent)
    }
}

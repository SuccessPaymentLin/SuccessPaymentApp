package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentgatewayui.databinding.ActivityAmountInputBinding
import java.util.UUID

class AmountInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmountInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmountInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNextAmount.setOnClickListener {
            val rawAmount = binding.editAmount.text.toString().trim()

            if (rawAmount.isEmpty()) {
                binding.editAmount.error = "Please enter an amount"
                return@setOnClickListener
            }

            // ✅ Format for backend/hash usage
            val backendAmount = when {
                rawAmount.contains(",") -> rawAmount.replace(",", ".")
                rawAmount.contains(".") -> rawAmount
                else -> "$rawAmount.00"
            }

            // 🔐 Static setup
            val entityKey = "02c4b39c-1b1b-4082-b961-d8b8a7c32417"
            val apiUsername = "success-api"
            val shaSecret = "paybylinksuccesspayment*"
            val mappingTemplate = "Test API"

            // 🔑 Generate short order ID (first 12 chars of UUID)
            val orderId = UUID.randomUUID().toString().take(12)

            // 🧾 Optional or required parameters by mapping
            val paymentRef = ""
            val g = ""
            val m = ""
            val n = ""
            val c = ""
            val pn = ""
            val ex = ""
            val l = ""

            // 🔗 Generate payment URL
            val paymentUrl = PayByLinkUtils.createPaymentUrl(
                amount = backendAmount,
                orderId = orderId,
                bk = paymentRef,
                licenseUUID = entityKey,
                mappingTemplate = mappingTemplate,
                u = apiUsername,
                shaSecret = shaSecret,
                g = g,
                m = m,
                n = n,
                c = c,
                pn = pn,
                ex = ex,
                l = l
            )

            // 🚀 Go to QR screen
            val intent = Intent(this, QrDisplayActivity::class.java).apply {
                putExtra("payment_url", paymentUrl)
            }
            startActivity(intent)
        }
    }
}
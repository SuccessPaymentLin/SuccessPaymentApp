package com.example.paymentgatewayui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentgatewayui.databinding.ActivityCardPaymentBinding

class CardPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Initialize Stripe PaymentSheet or CardInputWidget here

        binding.payButton.setOnClickListener {
            // Example placeholder logic
            // You can trigger your payment flow here
        }
    }
}
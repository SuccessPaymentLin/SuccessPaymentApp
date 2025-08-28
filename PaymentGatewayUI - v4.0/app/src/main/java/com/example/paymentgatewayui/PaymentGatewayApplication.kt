package com.example.paymentgatewayui

import android.app.Application
import android.util.Log

class PaymentGatewayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("AppInit", "PaymentGatewayApplication initialized")
        // Optionally initialize SDKs here (e.g., Wallee, OPI)
    }
}
package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.Stripe
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class StripePaymentActivity : AppCompatActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private lateinit var publishableKey: String
    private lateinit var clientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get amount passed via Intent
        val amount = intent.getStringExtra("amount") ?: "100"

        // Initialize PaymentSheet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // 🔁 Step 1: Fetch clientSecret from your Lambda backend
        fetchPaymentIntentFromLambda(amount)
    }

    private fun fetchPaymentIntentFromLambda(amount: String) {
        val json = JSONObject().apply {
            put("amount", amount)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://eqgvffm7w8.execute-api.eu-north-1.amazonaws.com/prod/stripe-payment") // 🔁 Replace with yours
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@StripePaymentActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    clientSecret = jsonResponse.getString("clientSecret")
                    publishableKey = jsonResponse.getString("publishableKey")

                    runOnUiThread {
                        presentPaymentSheet()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@StripePaymentActivity, "Failed to get payment intent", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        })
    }

    private fun presentPaymentSheet() {
        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "My App", // ✅ shows in Stripe sheet
        )

        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            configuration
        )
    }

    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // ✅ Payment success
                val intent = Intent(this, SuccessActivity::class.java)
                startActivity(intent)
                finish()
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment failed: ${result.error.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
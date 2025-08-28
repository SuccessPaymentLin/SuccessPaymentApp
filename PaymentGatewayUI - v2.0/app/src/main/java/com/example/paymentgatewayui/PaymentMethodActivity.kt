package com.example.paymentgatewayui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.Stripe
import com.stripe.android.PaymentConfiguration
import com.stripe.android.view.CardInputWidget
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.PaymentIntentResult
import com.stripe.android.ApiResultCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class PaymentMethodActivity : AppCompatActivity() {

    private lateinit var stripe: Stripe
    private lateinit var cardInputWidget: CardInputWidget
    private lateinit var payButton: Button
    private var currentClientSecret: String? = null
    private var isProcessingPayment = false

    // Create OkHttpClient with proper timeout settings
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PaymentMethodActivity", "onCreate called")

        // Initialize PaymentConfiguration FIRST, before any Stripe components are used
        val publishableKey = "pk_test_51RaeoYQN5wCivMbh0s01SshtzThTzeDpyqh7fFz4OGt5t1z60qg9Z1zIhrOp4E9aNTlYbQXtwhDwc2HI8tOujGzg00BWLmD2Y9"
        PaymentConfiguration.init(this, publishableKey)
        Log.d("PaymentMethodActivity", "PaymentConfiguration initialized")

        // Now set the content view (which will inflate the CardInputWidget)
        setContentView(R.layout.activity_payment_method)

        // Initialize Stripe with your publishable key
        stripe = Stripe(this, publishableKey)

        cardInputWidget = findViewById(R.id.cardInputWidget)
        payButton = findViewById(R.id.nextStepButton)

        // Get transaction details from intent
        val transactionType = intent.getStringExtra("transactionType") ?: "auth"
        val amount = intent.getStringExtra("amount")?.toIntOrNull() ?: 1500

        Log.d("PaymentMethodActivity", "Transaction type: $transactionType, Amount: $amount")

        payButton.setOnClickListener {
            if (isProcessingPayment) {
                Log.w("PaymentMethodActivity", "Payment already in progress, ignoring click")
                Toast.makeText(this, "Payment is already being processed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            processPayment(transactionType, amount)
        }
    }

    private fun processPayment(transactionType: String, amount: Int) {
        Log.d("PaymentMethodActivity", "Starting payment processing...")

        // Validate card input
        val cardParams = cardInputWidget.paymentMethodCreateParams
        if (cardParams == null) {
            Log.e("PaymentMethodActivity", "Card validation failed - no payment method params")
            Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if card details are complete
        if (!isCardInputValid()) {
            Log.e("PaymentMethodActivity", "Card validation failed - incomplete details")
            Toast.makeText(this, "Please check your card details", Toast.LENGTH_SHORT).show()
            return
        }

        setProcessingState(true)

        // Create payment intent using Lambda
        sendPaymentRequestToLambda(transactionType, amount, cardParams)
    }

    private fun isCardInputValid(): Boolean {
        // Basic validation - check if we can create payment method params
        return cardInputWidget.paymentMethodCreateParams != null
    }

    private fun setProcessingState(processing: Boolean) {
        isProcessingPayment = processing
        payButton.isEnabled = !processing
        payButton.text = if (processing) "Processing..." else "Pay Now"
        Log.d("PaymentMethodActivity", "Payment processing state: $processing")
    }

    private fun sendPaymentRequestToLambda(transactionType: String, amount: Int, cardParams: com.stripe.android.model.PaymentMethodCreateParams) {
        Log.d("PaymentMethodActivity", "Sending payment request to Lambda for $transactionType with amount $amount")

        val json = JSONObject().apply {
            put("amount", amount)
            put("currency", "usd")
            put("transaction_type", transactionType)
            put("customer_name", "Android Client")
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://eqgvffm7w8.execute-api.eu-north-1.amazonaws.com/prod/stripe-payment")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "PaymentGatewayApp/1.0")
            .build()

        Log.d("PaymentMethodActivity", "Sending request to Lambda: $json")

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PaymentMethodActivity", "Network request failed", e)
                runOnUiThread {
                    setProcessingState(false)
                    Toast.makeText(this@PaymentMethodActivity, "Network connection failed. Please check your internet connection.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("PaymentMethodActivity", "Lambda response code: ${response.code}")
                Log.d("PaymentMethodActivity", "Lambda response body: $responseBody")

                try {
                    if (!response.isSuccessful) {
                        Log.e("PaymentMethodActivity", "Server returned error: ${response.code} - $responseBody")
                        runOnUiThread {
                            setProcessingState(false)
                            Toast.makeText(this@PaymentMethodActivity, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    if (responseBody.isNullOrEmpty()) {
                        Log.e("PaymentMethodActivity", "Empty response from server")
                        runOnUiThread {
                            setProcessingState(false)
                            Toast.makeText(this@PaymentMethodActivity, "Empty response from server", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    val jsonResponse = JSONObject(responseBody)
                    val clientSecret = jsonResponse.optString("clientSecret")

                    if (clientSecret.isNotEmpty()) {
                        Log.d("PaymentMethodActivity", "Received client secret, confirming payment")
                        currentClientSecret = clientSecret
                        confirmPayment(cardParams, clientSecret)
                    } else {
                        Log.e("PaymentMethodActivity", "No client secret in response: $jsonResponse")
                        runOnUiThread {
                            setProcessingState(false)
                            Toast.makeText(this@PaymentMethodActivity, "Invalid server response - no client secret", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PaymentMethodActivity", "Error parsing server response", e)
                    runOnUiThread {
                        setProcessingState(false)
                        Toast.makeText(this@PaymentMethodActivity, "Error processing server response: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun confirmPayment(cardParams: com.stripe.android.model.PaymentMethodCreateParams, clientSecret: String) {
        Log.d("PaymentMethodActivity", "Confirming payment with Stripe using client secret: ${clientSecret.take(20)}...")

        val confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
            cardParams,
            clientSecret
        )

        runOnUiThread {
            stripe.confirmPayment(this@PaymentMethodActivity, confirmParams)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("PaymentMethodActivity", "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")

        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                Log.d("PaymentMethodActivity", "Stripe payment result received: ${result.intent.status}")
                handlePaymentResult(result)
            }

            override fun onError(e: Exception) {
                Log.e("PaymentMethodActivity", "Stripe payment error", e)
                setProcessingState(false)
                Toast.makeText(this@PaymentMethodActivity, "Payment processing failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handlePaymentResult(result: PaymentIntentResult) {
        Log.d("PaymentMethodActivity", "Handling payment result with status: ${result.intent.status}")

        when (result.intent.status) {
            StripeIntent.Status.Succeeded -> {
                Log.d("PaymentMethodActivity", "Payment succeeded, navigating to success screen")
                setProcessingState(false)
                val intent = Intent(this, SuccessActivity::class.java).apply {
                    putExtra("amount", result.intent.amount.toString())
                    putExtra("paymentIntentId", result.intent.id)
                    // Clear the task stack to prevent navigation loops
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
            StripeIntent.Status.RequiresPaymentMethod -> {
                Log.w("PaymentMethodActivity", "Payment requires new payment method")
                setProcessingState(false)
                val intent = Intent(this, FailureActivity::class.java).apply {
                    putExtra("error", "Payment method was declined")
                }
                startActivity(intent)
                finish()
            }
            StripeIntent.Status.Canceled -> {
                Log.d("PaymentMethodActivity", "Payment was canceled by user")
                setProcessingState(false)

                val intent = Intent(this, FailureActivity::class.java).apply {
                    putExtra("error", "Payment method was declined")
                }
                startActivity(intent)
                finish()
            }
            StripeIntent.Status.Processing -> {
                Log.d("PaymentMethodActivity", "Payment is still processing")
                Toast.makeText(this, "Payment is being processed. Please wait...", Toast.LENGTH_SHORT).show()
                // Don't reset processing state yet
            }
            else -> {
                Log.w("PaymentMethodActivity", "Unknown payment status: ${result.intent.status}")
                setProcessingState(false)
                Toast.makeText(this, "Payment completed with status: ${result.intent.status}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PaymentMethodActivity", "PaymentMethodActivity destroyed")
        // Cancel any ongoing network requests
        httpClient.dispatcher.executorService.shutdown()
    }

    override fun onBackPressed() {
        if (isProcessingPayment) {
            Toast.makeText(this, "Please wait for payment to complete", Toast.LENGTH_SHORT).show()
            return
        }
        super.onBackPressed()
    }
}
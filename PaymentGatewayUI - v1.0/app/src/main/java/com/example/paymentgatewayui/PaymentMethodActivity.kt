package com.example.paymentgatewayui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentgatewayui.sendPaymentRequestToLambda
import org.json.JSONObject
import android.util.Log

class PaymentMethodActivity : AppCompatActivity() {

    private lateinit var radioCard: RadioButton
    private lateinit var radioMobile: RadioButton
    private lateinit var providerGroup: LinearLayout
    private lateinit var radioMTN: RadioButton
    private lateinit var radioMoov: RadioButton
    private lateinit var radioOrange: RadioButton
    private lateinit var nextStepButton: Button

    private var selectedMethod: String? = null
    private var selectedProvider: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        radioCard = findViewById(R.id.radioCard)
        radioMobile = findViewById(R.id.radioMobile)
        providerGroup = findViewById(R.id.providerGroup)
        radioMTN = findViewById(R.id.radioMTN)
        radioMoov = findViewById(R.id.radioMoov)
        radioOrange = findViewById(R.id.radioOrange)
        nextStepButton = findViewById(R.id.nextStepButton)

        providerGroup.visibility = View.GONE

        radioCard.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedMethod = "card"
                providerGroup.visibility = View.GONE
            }
        }

        radioMobile.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedMethod = "mobile"
                providerGroup.visibility = View.VISIBLE
            }
        }

        nextStepButton.setOnClickListener {
            if (selectedMethod == null) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedMethod == "mobile") {
                selectedProvider = when {
                    radioMTN.isChecked -> "MTN"
                    radioMoov.isChecked -> "Moov"
                    radioOrange.isChecked -> "Orange"
                    else -> null
                }

                if (selectedProvider == null) {
                    Toast.makeText(this, "Please select a provider", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val transactionType = intent.getStringExtra("transactionType")
            val amount = intent.getStringExtra("amount")?.toIntOrNull() ?: 0

            val lambdaUrl = "https://axwv0naora.execute-api.eu-north-1.amazonaws.com/prod/FetchBictorysQR"

            sendPaymentRequestToLambda(
                context = this,
                lambdaUrl = lambdaUrl,
                paymentType = selectedProvider?.lowercase() + "_money",
                amount = amount,
                customerName = "Linda",
                onSuccess = { result ->
                    Log.d("LambdaResponse", "Lambda raw result: $result")

                    val jsonObject = JSONObject(result)
                    val qrBase64 = jsonObject.optString("qrCode")
                    val verificationLink = jsonObject.optString("verificationLink")

                    if (!qrBase64.isNullOrEmpty()) {
                        val qrIntent = Intent(this, QRCodeActivity::class.java)
                        qrIntent.putExtra("qrCodeBase64", qrBase64)
                        qrIntent.putExtra("verificationLink", verificationLink)
                        Log.d("DEBUG", "Verification link: $verificationLink")
                        startActivity(qrIntent)
                    } else {
                        Log.e("QRCodeIntent", "qrCode missing. Full JSON: $result")
                        Toast.makeText(this, "Failed to get QR code image.", Toast.LENGTH_LONG).show()
                    }
                },
                onError = { error ->
                    Log.e("LambdaError", "Lambda error: $error")
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
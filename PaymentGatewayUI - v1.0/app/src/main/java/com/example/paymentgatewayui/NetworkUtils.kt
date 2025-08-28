package com.example.paymentgatewayui

import android.app.Activity
import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

fun sendPaymentRequestToLambda(
    context: Context,
    lambdaUrl: String,
    paymentType: String,
    amount: Int,
    customerName: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val client = OkHttpClient()

    val jsonBody = JSONObject().apply {
        put("payment_type", paymentType)
        put("amount", amount)
        put("merchantReference", "tx-android-${System.currentTimeMillis()}")
        put("paymentReference", "xref-${System.currentTimeMillis()}")
        put("redirectUrl", "https://example.com/success-payment")
        put("currency", "XOF")
        put("customer", JSONObject().apply {
            put("name", customerName)
            put("phone", "+2250102030405")
            put("email", "client@example.com")
            put("city", "Abidjan")
            put("postal_code", "22500")
            put("country", "CI")
            put("locale", "fr-FR")
        })
        put("orderDetails", JSONArray().apply {
            put(JSONObject().apply {
                put("name", "Payment")
                put("price", amount)
                put("quantity", 1)
                put("taxRate", 0)
            })
        })
    }

    val request = Request.Builder()
        .url(lambdaUrl)
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            (context as Activity).runOnUiThread {
                onError(e.message ?: "Unknown error")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseText = response.body?.string() ?: ""
            (context as Activity).runOnUiThread {
                if (response.isSuccessful) {
                    onSuccess(responseText)
                } else {
                    onError("HTTP ${response.code}: $responseText")
                }
            }
        }
    })
}
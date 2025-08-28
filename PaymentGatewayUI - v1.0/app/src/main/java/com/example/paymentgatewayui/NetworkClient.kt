package com.example.paymentgatewayui

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

object NetworkClient {
    private val client = OkHttpClient()

    fun sendPayment(amount: String, callback: (Boolean) -> Unit) {
        val url = "http://16.171.115.204:8080/charge" // your backend endpoint

        val json = """
            {
              "amount": "$amount",
              "description": "Test charge"
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful)
            }
        })
    }
}
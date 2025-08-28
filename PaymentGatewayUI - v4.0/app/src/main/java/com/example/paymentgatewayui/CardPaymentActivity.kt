package com.example.paymentgatewayui

import android.os.Bundle
import android.util.Log
import android.view.View                    // ⬅ add
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.paymentgatewayui.databinding.ActivityCardPaymentBinding
import com.example.paymentgatewayui.utils.Constants.BASE_URL
import org.json.JSONObject

class CardPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide success logo on first load
        binding.successLogo.visibility = View.VISIBLE          // ⬅ add

        // prove connectivity first
        pingHealth()

        binding.payButton.setOnClickListener {
            // fresh attempt: hide logo and start call
            binding.successLogo.visibility = View.GONE      // ⬅ add
            initiatePayment()
        }
    }

    private fun pingHealth() {
        val healthUrl = "$BASE_URL/health"
        Log.i("PAYMENT", "Pinging: $healthUrl")

        val req = StringRequest(
            Request.Method.GET,
            healthUrl,
            { resp -> Log.i("PAYMENT", "Health OK: $resp") },
            { err ->
                Log.e("PAYMENT", "Health ERR", err)
                err.networkResponse?.let {
                    Log.e("PAYMENT", "HTTP ${it.statusCode} body=${String(it.data)}")
                } ?: Log.e("PAYMENT", "No network response (timeout/DNS/SSL)")
            }
        ).apply {
            retryPolicy = DefaultRetryPolicy(30_000, 0, 1.0f)
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun initiatePayment() {
        val url = "$BASE_URL/api/payment/start"
        Log.i("PAYMENT", "Calling: $url")

        // disable button while calling (optional but nice)
        binding.payButton.isEnabled = false                 // ⬅ add

        // match the middleware JSON exactly
        val bodyJson = JSONObject().apply {
            put("amount", 50.0)
            put("transactionType", "SALE")
            put("bookingReference", "OPERA-123")
            put("terminalId", "32580601")
            put("currency", "EUR")
        }.toString()

        val req = object : StringRequest(
            Request.Method.POST,
            url,
            { resp ->
                Log.i("PAYMENT", "Payment OK: $resp")
                Toast.makeText(this, "Payment started: $resp", Toast.LENGTH_SHORT).show()

                // ⬇ show the success logo only when backend says SUCCESS
                if (resp.contains("SUCCESS", ignoreCase = true)) {
                    binding.successLogo.visibility = View.VISIBLE
                } else {
                    binding.successLogo.visibility = View.GONE
                }

                binding.payButton.isEnabled = true          // ⬅ add
            },
            { err ->
                Log.e("PAYMENT", "Payment ERR", err)
                err.networkResponse?.let {
                    Log.e("PAYMENT", "HTTP ${it.statusCode} body=${String(it.data)}")
                } ?: Log.e("PAYMENT", "No network response (timeout/DNS/SSL)")
                Toast.makeText(this, "Payment failed: ${err.message}", Toast.LENGTH_LONG).show()

                binding.successLogo.visibility = View.GONE  // ⬅ add
                binding.payButton.isEnabled = true          // ⬅ add
            }
        ) {
            override fun getBody(): ByteArray = bodyJson.toByteArray(Charsets.UTF_8)
            override fun getBodyContentType(): String = "application/json"
            override fun getHeaders(): MutableMap<String, String> =
                mutableMapOf("Content-Type" to "application/json") // no Basic auth in dev
        }.apply {
            retryPolicy = DefaultRetryPolicy(60_000, 0, 1.0f)
        }

        Volley.newRequestQueue(this).add(req)
    }
}
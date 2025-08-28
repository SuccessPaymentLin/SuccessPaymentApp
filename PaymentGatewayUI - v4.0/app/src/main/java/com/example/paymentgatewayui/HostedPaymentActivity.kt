package com.example.paymentgatewayui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.paymentgatewayui.databinding.ActivityHostedPaymentBinding

class HostedPaymentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "payment_url"
    }

    private lateinit var binding: ActivityHostedPaymentBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostedPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paymentUrl = intent.getStringExtra(EXTRA_URL)

        if (paymentUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Missing payment URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false

                    Log.d("HostedPayment", "Navigating to: $url")

                    return when {
                        url.contains("success", true) -> {
                            startActivity(Intent(this@HostedPaymentActivity, SuccessActivity::class.java))
                            finish()
                            true
                        }
                        url.contains("failure", true) || url.contains("error", true) -> {
                            startActivity(Intent(this@HostedPaymentActivity, FailureActivity::class.java))
                            finish()
                            true
                        }
                        else -> false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("HostedPayment", "Page finished loading: $url")
                }
            }

            loadUrl(paymentUrl)
        }
    }
}
package com.example.paymentgatewayui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class QrDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_display)

        val qrImageView = findViewById<ImageView>(R.id.qrCodeImage)
        val webView = findViewById<WebView>(R.id.paymentWebView)
        val resultText = findViewById<TextView>(R.id.resultText)

        val paymentUrl = intent.getStringExtra("payment_url")

        if (paymentUrl.isNullOrEmpty()) {
            Toast.makeText(this, "No QR data received", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            val qrBitmap = generateQrCode(paymentUrl)
            qrImageView.setImageBitmap(qrBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
        }

        webView.settings.javaScriptEnabled = true
        webView.visibility = View.GONE
        resultText.visibility = View.GONE

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    when {
                        it.contains("success", ignoreCase = true) -> {
                            resultText.text = "✅ Payment Successful"
                            resultText.visibility = View.VISIBLE
                            webView.visibility = View.GONE
                            return true
                        }

                        it.contains("fail", ignoreCase = true) || it.contains("cancel", ignoreCase = true) -> {
                            resultText.text = "❌ Payment Declined"
                            resultText.visibility = View.VISIBLE
                            webView.visibility = View.GONE
                            return true
                        }
                    }
                }
                return false
            }
        }

        // Load the URL in background to track the redirect
        webView.loadUrl(paymentUrl)
    }

    private fun generateQrCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 800, 800)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bitmap
    }
}
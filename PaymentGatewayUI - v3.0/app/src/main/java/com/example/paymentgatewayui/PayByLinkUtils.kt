package com.example.paymentgatewayui

import java.security.MessageDigest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Log

object PayByLinkUtils {

    fun createPaymentUrl(
        amount: String,
        orderId: String,
        bk: String = "",
        licenseUUID: String = "02c4b39c-1b1b-4082-b961-d8b8a7c32417",
        mappingTemplate: String = "Test API",
        u: String = "success-api",
        shaSecret: String = "paybylinksuccesspayment*",
        g: String = "",
        m: String = "",
        n: String = "",
        c: String = "",
        pn: String = "",
        ex: String = "",
        l: String = ""
    ): String {
        val formattedAmount = amount.replace(".", ",")

        val hashInput = licenseUUID +
                mappingTemplate +
                u +
                formattedAmount +
                orderId +
                g + m + n + c + pn + ex + l +
                shaSecret

        Log.d("HashDebug", "Hash input string: $hashInput")

        val sha256 = sha256(hashInput).uppercase()

        // URL encode all fields
        val mtEncoded = URLEncoder.encode(mappingTemplate, StandardCharsets.UTF_8.toString())
        val uEncoded = URLEncoder.encode(u, StandardCharsets.UTF_8.toString())
        val aEncoded = URLEncoder.encode(formattedAmount, StandardCharsets.UTF_8.toString())
        val orEncoded = URLEncoder.encode(orderId, StandardCharsets.UTF_8.toString())
        val bkEncoded = URLEncoder.encode(bk, StandardCharsets.UTF_8.toString())
        val gEncoded = URLEncoder.encode(g, StandardCharsets.UTF_8.toString())
        val mEncoded = URLEncoder.encode(m, StandardCharsets.UTF_8.toString())
        val nEncoded = URLEncoder.encode(n, StandardCharsets.UTF_8.toString())
        val cEncoded = URLEncoder.encode(c, StandardCharsets.UTF_8.toString())
        val pnEncoded = URLEncoder.encode(pn, StandardCharsets.UTF_8.toString())
        val exEncoded = URLEncoder.encode(ex, StandardCharsets.UTF_8.toString())
        val lEncoded = URLEncoder.encode(l, StandardCharsets.UTF_8.toString())

        val url = "https://testapi.paybylink.com/payment/createUrl/$licenseUUID" +
                "?mt=$mtEncoded" +
                "&u=$uEncoded" +
                "&a=$aEncoded" +
                "&or=$orEncoded" +
                "&bk=$bkEncoded" +
                "&g=$gEncoded" +
                "&m=$mEncoded" +
                "&n=$nEncoded" +
                "&c=$cEncoded" +
                "&pn=$pnEncoded" +
                "&ex=$exEncoded" +
                "&l=$lEncoded" +
                "&h=$sha256"

        Log.d("Generated URL", url)
        return url
    }

    private fun sha256(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray(Charsets.UTF_8))
            val result = bytes.joinToString("") { "%02X".format(it) }
            Log.d("HashDebug", "Hash output: $result")
            result
        } catch (e: Exception) {
            Log.e("HashDebug", "SHA256 generation failed", e)
            "ERROR"
        }
    }
}
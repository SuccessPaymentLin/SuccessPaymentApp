package com.example.paymentgatewayui.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object LogUtils {

    fun writeTransactionLog(context: Context, transactionType: String, status: String, responseCode: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

            val logJson = JSONObject().apply {
                put("timestamp", timestamp)
                put("transactionType", transactionType)
                put("status", status)
                put("responseCode", responseCode)
            }

            val logsDir = File(context.getExternalFilesDir(null), "opi_logs")
            if (!logsDir.exists()) logsDir.mkdirs()

            val logFile = File(logsDir, "log_${System.currentTimeMillis()}.json")
            FileWriter(logFile).use { it.write(logJson.toString(4)) }

            Log.d("LogUtils", "Transaction log saved: ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("LogUtils", "Failed to write log: ${e.message}")
        }
    }
}
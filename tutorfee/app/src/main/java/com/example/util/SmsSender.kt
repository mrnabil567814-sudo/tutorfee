package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat

object SmsSender {

    /**
     * Sends SMS directly using SmsManager if permission is granted, otherwise opens the default SMS app.
     */
    fun sendOrDraftSms(
        context: Context,
        recipientPhone: String,
        messageText: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val hasSendSmsPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasSendSmsPermission) {
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(messageText)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(recipientPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(recipientPhone, null, messageText, null, null)
                }

                Toast.makeText(context, "SMS sent to $recipientPhone", Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            } catch (e: Exception) {
                Toast.makeText(context, "Direct send failed: ${e.localizedMessage}. Opening SMS app...", Toast.LENGTH_SHORT).show()
                openSmsApp(context, recipientPhone, messageText)
                onError?.invoke(e.localizedMessage ?: "Failed to send SMS")
            }
        } else {
            openSmsApp(context, recipientPhone, messageText)
            onSuccess?.invoke()
        }
    }

    /**
     * Opens the device SMS compose screen with prefilled recipient and message.
     */
    fun openSmsApp(context: Context, recipientPhone: String, messageText: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$recipientPhone")
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Copies message text to system clipboard.
     */
    fun copyToClipboard(context: Context, messageText: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Payment Receipt", messageText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "SMS text copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

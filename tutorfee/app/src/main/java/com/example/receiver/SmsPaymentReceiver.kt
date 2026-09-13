package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.TutorRepository
import com.example.util.SmsParser
import com.example.util.SmsSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsPaymentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fullBody = buildString {
                    for (sms in messages) {
                        append(sms.displayMessageBody)
                    }
                }
                val sender = messages[0].displayOriginatingAddress ?: ""

                if (SmsParser.isPaymentSms(sender, fullBody)) {
                    val parsed = SmsParser.parse(sender, fullBody)
                    if (parsed != null) {
                        val repository = TutorRepository.getInstance(context)
                        val result = repository.processIncomingSmsPayment(parsed)

                        if (result != null) {
                            val (student, payment) = result
                            val tutorProfile = repository.getTutorProfile()
                            val confirmationSms = repository.generateConfirmationSms(
                                studentName = student.name,
                                amount = payment.amount,
                                tutorName = tutorProfile.tutorName,
                                remainingDue = student.currentDueBalance
                            )

                            // Post to Event Bus for active Compose UI
                            PaymentEventBus.emitEvent(
                                AutoPaymentEvent(
                                    student = student,
                                    payment = payment,
                                    confirmationSms = confirmationSms
                                )
                            )

                            // If auto-send is enabled in tutor settings, send confirmation directly
                            if (tutorProfile.autoSendSms) {
                                SmsSender.sendOrDraftSms(
                                    context = context,
                                    recipientPhone = student.phone,
                                    messageText = confirmationSms
                                )
                            }

                            // Show local status notification
                            showPaymentNotification(
                                context = context,
                                studentName = student.name,
                                amount = payment.amount,
                                method = payment.paymentMethod,
                                remainingDue = student.currentDueBalance
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore unexpected parsing/broadcast issues
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showPaymentNotification(
        context: Context,
        studentName: String,
        amount: Double,
        method: String,
        remainingDue: Double
    ) {
        val channelId = "tutor_fee_payments"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fee Payments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for student fee payments"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dueText = if (remainingDue <= 0.0) "Fully Paid!" else "${remainingDue.toInt()} Tk"
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Payment Received: $studentName ($method)")
            .setContentText("${amount.toInt()} Tk credited. Remaining Due: $dueText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Received ${amount.toInt()} Tk from $studentName via $method. Remaining balance is $dueText.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

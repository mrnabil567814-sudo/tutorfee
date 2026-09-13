package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.PaymentMethodChip
import com.example.ui.theme.Green100
import com.example.ui.theme.Green700
import com.example.ui.viewmodel.SmsDraftData
import com.example.util.SmsSender

@Composable
fun SmsDraftDialog(
    draftData: SmsDraftData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editableMessage by remember { mutableStateOf(draftData.messageText) }

    // Runtime permission launcher for SEND_SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            SmsSender.sendOrDraftSms(
                context = context,
                recipientPhone = draftData.studentPhone,
                messageText = editableMessage,
                onSuccess = { onDismiss() }
            )
        } else {
            // Fallback to opening system SMS app
            SmsSender.openSmsApp(
                context = context,
                recipientPhone = draftData.studentPhone,
                messageText = editableMessage
            )
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = Green700,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Confirmation SMS Draft",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Receipt generated for student",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Recipient header banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = draftData.studentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = draftData.studentPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    PaymentMethodChip(method = draftData.paymentMethod)
                }

                Text(
                    text = "SMS Body Preview (Editable):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = editableMessage,
                    onValueChange = { editableMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sms_draft_content_input"),
                    minLines = 3,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // Quick copy button
                OutlinedButton(
                    onClick = {
                        SmsSender.copyToClipboard(context, editableMessage)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("copy_sms_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy SMS to Clipboard", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.SEND_SMS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        SmsSender.sendOrDraftSms(
                            context = context,
                            recipientPhone = draftData.studentPhone,
                            messageText = editableMessage,
                            onSuccess = { onDismiss() }
                        )
                    } else {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                },
                modifier = Modifier.testTag("send_sms_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send SMS")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    SmsSender.openSmsApp(
                        context = context,
                        recipientPhone = draftData.studentPhone,
                        messageText = editableMessage
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("open_sms_app_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("SMS App")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

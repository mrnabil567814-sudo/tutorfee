package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.TutorQrCodeDisplay

@Composable
fun QrPreviewDialog(
    qrCodePath: String,
    tutorName: String,
    tutorPhone: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Tutor Payment QR Code",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (tutorName.isNotBlank() || tutorPhone.isNotBlank()) {
                    Text(
                        text = "$tutorName ${if (tutorPhone.isNotBlank()) "($tutorPhone)" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TutorQrCodeDisplay(
                    qrCodePath = qrCodePath,
                    modifier = Modifier.fillMaxWidth(),
                    sizeDp = 220
                )
                if (qrCodePath.isBlank()) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenSettings()
                        },
                        modifier = Modifier.testTag("setup_qr_from_preview_button")
                    ) {
                        Text("Open Settings to Upload QR Code")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_qr_preview_button")
            ) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

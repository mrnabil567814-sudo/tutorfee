package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.viewmodel.TutorViewModel

@Composable
fun SmsSimulatorDialog(
    viewModel: TutorViewModel,
    onDismiss: () -> Unit
) {
    var sender by remember { mutableStateOf("bKash") }
    var smsText by remember {
        mutableStateOf("You have received Tk 2,000.00 from 01711223344. Ref Fee Tk 0.00. Balance Tk 15,250.00. TrxID 9H82KA71")
    }
    var simulationResult by remember { mutableStateOf<String?>(null) }

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
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "SMS Payment Reader Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Test incoming bKash & Nagad SMS detection",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Quick Sample Templates:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            sender = "bKash"
                            smsText = "You have received Tk 2,000.00 from 01711223344. Ref Fee Tk 0.00. Balance Tk 15,250.00. TrxID 9H82KA71"
                            simulationResult = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("bKash (2000)", fontSize = 11.sp, color = BkashPink)
                    }

                    OutlinedButton(
                        onClick = {
                            sender = "16167"
                            smsText = "Cash In of Tk 2,500.00 from 01822334455 successful. Ref: Tuition TxnID: 76HJK29 Balance: Tk 12,000.00."
                            simulationResult = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nagad (2500)", fontSize = 11.sp, color = NagadOrange)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            sender = "bKash"
                            smsText = "Cash In Tk 900.00 from 01933445566 successful. TrxID 8G23JK90 Balance Tk 4,500.00"
                            simulationResult = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Partial (900)", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            sender = "bKash"
                            smsText = "You have received Tk 3,000.00 from 01644556677. Ref Fee Tk 0.00. TrxID 3KA87G9"
                            simulationResult = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("bKash (3000)", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("SMS Sender ID") },
                    placeholder = { Text("bKash or 16167") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sms_sender_input")
                )

                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    label = { Text("Incoming SMS Body") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sms_body_input"),
                    minLines = 3,
                    maxLines = 4
                )

                if (simulationResult != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Detection Result:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = simulationResult ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.testSimulateSms(sender, smsText) { result ->
                        simulationResult = result
                    }
                },
                modifier = Modifier.testTag("run_simulation_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text("Process & Match")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_simulation_button")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

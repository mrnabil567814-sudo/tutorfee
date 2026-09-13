package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.TutorQrCodeDisplay
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CashGreen
import com.example.ui.theme.Green100
import com.example.ui.theme.Green700
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.Red100
import com.example.ui.theme.Red700

@Composable
fun RecordPaymentDialog(
    student: Student,
    qrCodePath: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (
        amount: Double,
        method: String,
        transactionId: String,
        notes: String
    ) -> Unit
) {
    val initialAmount = if (student.currentDueBalance > 0) {
        if (student.currentDueBalance % 1.0 == 0.0) student.currentDueBalance.toInt().toString() else student.currentDueBalance.toString()
    } else {
        if (student.monthlyFee % 1.0 == 0.0) student.monthlyFee.toInt().toString() else student.monthlyFee.toString()
    }

    var amountText by remember { mutableStateOf(initialAmount) }
    var selectedMethod by remember { mutableStateOf("bKash") }
    var transactionId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showQrCode by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val newDue = (student.currentDueBalance - enteredAmount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Record Fee Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "For: ${student.name} (${student.phone})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance summary card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Due Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${student.currentDueBalance.toInt()} Tk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (student.currentDueBalance > 0) Red700 else Green700
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "New Due After Pay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${newDue.toInt()} Tk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (newDue <= 0.0) Green700 else Red700
                        )
                    }
                }

                // Payment Method Selector
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("bKash" to BkashPink, "Nagad" to NagadOrange, "Cash" to CashGreen).forEach { (method, color) ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = method,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = null
                    },
                    label = { Text("Payment Amount (Tk) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } } ?: {
                        Text("Supports full or partial payment")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (student.currentDueBalance > 0) {
                        OutlinedButton(
                            onClick = {
                                amountText = student.currentDueBalance.toInt().toString()
                                amountError = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Full Due (${student.currentDueBalance.toInt()} Tk)", fontSize = 11.sp)
                        }
                    }

                    if (student.monthlyFee > 0 && student.monthlyFee != student.currentDueBalance) {
                        OutlinedButton(
                            onClick = {
                                amountText = student.monthlyFee.toInt().toString()
                                amountError = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("1 Month (${student.monthlyFee.toInt()} Tk)", fontSize = 11.sp)
                        }
                    }
                }

                // Transaction ID if MFS
                if (selectedMethod == "bKash" || selectedMethod == "Nagad") {
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Transaction ID (Optional)") },
                        placeholder = { Text("e.g. 9H78GA67") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_trx_id_input")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Payment Note (Optional)") },
                    placeholder = { Text("e.g. September month fee") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_notes_input")
                )

                // Toggle QR code setup
                OutlinedButton(
                    onClick = { showQrCode = !showQrCode },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showQrCode) "Hide Tutor QR Code" else "Show Tutor QR Code to Scan")
                    Icon(
                        imageVector = if (showQrCode) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = showQrCode) {
                    TutorQrCodeDisplay(
                        qrCodePath = qrCodePath,
                        modifier = Modifier.fillMaxWidth(),
                        sizeDp = 160
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        amountError = "Enter a valid amount"
                        return@Button
                    }
                    onConfirmPayment(amt, selectedMethod, transactionId, notes)
                },
                modifier = Modifier.testTag("confirm_record_payment_button")
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_record_payment_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

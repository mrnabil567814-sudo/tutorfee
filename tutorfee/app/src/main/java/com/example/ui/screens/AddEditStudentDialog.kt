package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Student

@Composable
fun AddEditStudentDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        phone: String,
        monthlyFee: Double,
        dueDayOfMonth: Int,
        currentDueBalance: Double,
        notes: String
    ) -> Unit
) {
    val isEditing = student != null

    var name by remember { mutableStateOf(student?.name ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var monthlyFee by remember {
        mutableStateOf(
            if (student != null) {
                if (student.monthlyFee % 1.0 == 0.0) student.monthlyFee.toInt().toString() else student.monthlyFee.toString()
            } else "2000"
        )
    }
    var dueDayOfMonth by remember {
        mutableStateOf(student?.dueDayOfMonth?.toString() ?: "10")
    }
    var currentDueBalance by remember {
        mutableStateOf(
            if (student != null) {
                if (student.currentDueBalance % 1.0 == 0.0) student.currentDueBalance.toInt().toString() else student.currentDueBalance.toString()
            } else "2000"
        )
    }
    var notes by remember { mutableStateOf(student?.notes ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var feeError by remember { mutableStateOf<String?>(null) }
    var dayError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Student Profile" else "Add New Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Student Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = { Text("Mobile Number (bKash/Nagad) *") },
                    placeholder = { Text("01712345678") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } } ?: {
                        Text("Used for auto SMS matching & receipts")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_phone_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = monthlyFee,
                        onValueChange = {
                            monthlyFee = it
                            feeError = null
                            if (!isEditing) {
                                currentDueBalance = it
                            }
                        },
                        label = { Text("Monthly Fee (Tk) *") },
                        leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = feeError != null,
                        supportingText = feeError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("monthly_fee_input")
                    )

                    OutlinedTextField(
                        value = dueDayOfMonth,
                        onValueChange = {
                            dueDayOfMonth = it
                            dayError = null
                        },
                        label = { Text("Due Day (1-31) *") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = dayError != null,
                        supportingText = dayError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("due_day_input")
                    )
                }

                OutlinedTextField(
                    value = currentDueBalance,
                    onValueChange = { currentDueBalance = it },
                    label = { Text("Current Due Balance (Tk)") },
                    supportingText = {
                        Text("Set cumulative past unpaid fees if any")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_due_balance_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Batch / Subject Notes") },
                    placeholder = { Text("e.g. Class 10 Math, 3 days/week") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = "Student name is required"
                        hasError = true
                    }
                    if (phone.isBlank()) {
                        phoneError = "Phone number is required"
                        hasError = true
                    }
                    val feeVal = monthlyFee.toDoubleOrNull()
                    if (feeVal == null || feeVal <= 0) {
                        feeError = "Enter valid fee"
                        hasError = true
                    }
                    val dayVal = dueDayOfMonth.toIntOrNull()
                    if (dayVal == null || dayVal !in 1..31) {
                        dayError = "1 to 31"
                        hasError = true
                    }
                    val balanceVal = currentDueBalance.toDoubleOrNull() ?: 0.0

                    if (!hasError) {
                        onSave(
                            student?.id ?: 0L,
                            name,
                            phone,
                            feeVal ?: 2000.0,
                            dayVal ?: 10,
                            balanceVal,
                            notes
                        )
                    }
                },
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Add Student")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_student_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

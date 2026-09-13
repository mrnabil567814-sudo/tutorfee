package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.ui.components.AmountBadge
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.Green600
import com.example.ui.theme.Green700
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Red600
import com.example.ui.theme.Red700
import com.example.ui.viewmodel.StudentFilter
import com.example.ui.viewmodel.TutorViewModel
import com.example.util.PhoneUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TutorViewModel) {
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle(initialValue = emptyList())
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle(initialValue = emptyList())
    val tutorProfile by viewModel.tutorProfile.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    val activeDraftSms by viewModel.activeDraftSms.collectAsStateWithLifecycle()
    val showAddEditDialog by viewModel.showAddEditDialog.collectAsStateWithLifecycle()
    val addEditStudent by viewModel.addEditStudent.collectAsStateWithLifecycle()
    val paymentTargetStudent by viewModel.paymentTargetStudent.collectAsStateWithLifecycle()
    val selectedStudentDetails by viewModel.selectedStudentDetails.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()
    val showSmsSimulator by viewModel.showSmsSimulator.collectAsStateWithLifecycle()
    val showQrPreview by viewModel.showQrPreview.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // If student details screen is active
    if (selectedStudentDetails != null) {
        val currentStudent = allStudents.find { it.id == selectedStudentDetails?.id } ?: selectedStudentDetails!!
        StudentDetailScreen(
            student = currentStudent,
            viewModel = viewModel,
            onBack = { viewModel.closeStudentDetails() }
        )
        return
    }

    // If settings screen is active
    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            profile = tutorProfile,
            onBack = { viewModel.closeSettings() }
        )
        return
    }

    // Calculations for KPI Cards
    val totalDueAmount = allStudents.sumOf { if (it.currentDueBalance > 0) it.currentDueBalance else 0.0 }
    val totalCollected = allPayments.sumOf { it.amount }
    val unpaidCount = allStudents.count { !it.isFullyPaid }
    val paidCount = allStudents.count { it.isFullyPaid }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TutorFee",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "${tutorProfile.tutorName} • Fee Management",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openQrPreview() },
                        modifier = Modifier.testTag("open_qr_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Tutor QR Code"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openSmsSimulator() },
                        modifier = Modifier.testTag("open_sms_simulator_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS Reader Simulator"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddStudentDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // KPI Summary Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "TOTAL DUE",
                        value = "${totalDueAmount.toInt()} Tk",
                        subtitle = "$unpaidCount student${if (unpaidCount == 1) "" else "s"} pending",
                        accentColor = Red600,
                        icon = Icons.Default.Warning,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_total_due")
                    )

                    StatCard(
                        title = "COLLECTED",
                        value = "${totalCollected.toInt()} Tk",
                        subtitle = "$paidCount fully paid",
                        accentColor = Green600,
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_total_collected")
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input"),
                    placeholder = { Text("Search by name or mobile number...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == StudentFilter.ALL,
                            onClick = { viewModel.onFilterSelected(StudentFilter.ALL) },
                            label = { Text("All Students (${allStudents.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == StudentFilter.UNPAID_ONLY,
                            onClick = { viewModel.onFilterSelected(StudentFilter.UNPAID_ONLY) },
                            label = { Text("Due / Unpaid ($unpaidCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Red600.copy(alpha = 0.15f),
                                selectedLabelColor = Red700
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == StudentFilter.PAID_ONLY,
                            onClick = { viewModel.onFilterSelected(StudentFilter.PAID_ONLY) },
                            label = { Text("Fully Paid ($paidCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green600.copy(alpha = 0.15f),
                                selectedLabelColor = Green700
                            )
                        )
                    }
                }
            }

            // Students List
            if (filteredStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No students match '$searchQuery'" else "No students added yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button below to add your first student",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentCard(
                        student = student,
                        onCardClick = { viewModel.openStudentDetails(student) },
                        onRecordPaymentClick = { viewModel.openRecordPaymentDialog(student) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
            }
        }
    }

    // Dialogs
    if (showAddEditDialog) {
        AddEditStudentDialog(
            student = addEditStudent,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, name, phone, fee, dueDay, balance, notes ->
                viewModel.saveStudent(id, name, phone, fee, dueDay, balance, notes)
            }
        )
    }

    if (paymentTargetStudent != null) {
        RecordPaymentDialog(
            student = paymentTargetStudent!!,
            qrCodePath = tutorProfile.qrCodePath,
            onDismiss = { viewModel.closeRecordPaymentDialog() },
            onConfirmPayment = { amount, method, trxId, notes ->
                viewModel.recordManualPayment(
                    studentId = paymentTargetStudent!!.id,
                    amount = amount,
                    method = method,
                    transactionId = trxId,
                    notes = notes
                )
            }
        )
    }

    if (activeDraftSms != null) {
        SmsDraftDialog(
            draftData = activeDraftSms!!,
            onDismiss = { viewModel.dismissDraftSms() }
        )
    }

    if (showSmsSimulator) {
        SmsSimulatorDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSmsSimulator() }
        )
    }

    if (showQrPreview) {
        QrPreviewDialog(
            qrCodePath = tutorProfile.qrCodePath,
            tutorName = tutorProfile.tutorName,
            tutorPhone = tutorProfile.tutorPhone,
            onDismiss = { viewModel.closeQrPreview() },
            onOpenSettings = { viewModel.openSettings() }
        )
    }
}

@Composable
fun StudentCard(
    student: Student,
    onCardClick: () -> Unit,
    onRecordPaymentClick: () -> Unit
) {
    val status = student.computeStatus()
    val isPaid = student.isFullyPaid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("student_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = PhoneUtils.formatDisplay(student.phone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusBadge(status = status)
            }

            // Body: Due Balance & Monthly Fee
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isPaid) "0 Tk (Fully Paid)" else "${student.currentDueBalance.toInt()} Tk Due",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPaid) Green700 else Red700
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly: ${student.monthlyFee.toInt()} Tk",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Due Day: ${student.dueDayOfMonth}th",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Footer: Notes & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (student.notes.isNotBlank()) {
                    Text(
                        text = student.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = onRecordPaymentClick,
                    modifier = Modifier.testTag("record_payment_card_button_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Pay", fontSize = 12.sp)
                }
            }
        }
    }
}

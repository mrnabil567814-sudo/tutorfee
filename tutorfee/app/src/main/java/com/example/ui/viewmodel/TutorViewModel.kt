package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TutorProfile
import com.example.data.repository.TutorRepository
import com.example.receiver.PaymentEventBus
import com.example.util.SmsParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class StudentFilter {
    ALL,
    UNPAID_ONLY,
    PAID_ONLY
}

data class SmsDraftData(
    val studentName: String,
    val studentPhone: String,
    val amountPaid: Double,
    val remainingDue: Double,
    val messageText: String,
    val paymentMethod: String
)

class TutorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TutorRepository.getInstance(application)

    val allStudents = repository.allStudents
    val allPayments = repository.allPayments
    val tutorProfile = repository.tutorProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.getTutorProfile()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(StudentFilter.ALL)
    val selectedFilter: StateFlow<StudentFilter> = _selectedFilter.asStateFlow()

    private val _activeDraftSms = MutableStateFlow<SmsDraftData?>(null)
    val activeDraftSms: StateFlow<SmsDraftData?> = _activeDraftSms.asStateFlow()

    private val _addEditStudent = MutableStateFlow<Student?>(null)
    val addEditStudent: StateFlow<Student?> = _addEditStudent.asStateFlow()
    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _paymentTargetStudent = MutableStateFlow<Student?>(null)
    val paymentTargetStudent: StateFlow<Student?> = _paymentTargetStudent.asStateFlow()

    private val _selectedStudentDetails = MutableStateFlow<Student?>(null)
    val selectedStudentDetails: StateFlow<Student?> = _selectedStudentDetails.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showSmsSimulator = MutableStateFlow(false)
    val showSmsSimulator: StateFlow<Boolean> = _showSmsSimulator.asStateFlow()

    private val _showQrPreview = MutableStateFlow(false)
    val showQrPreview: StateFlow<Boolean> = _showQrPreview.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Filtered students flow
    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        _searchQuery,
        _selectedFilter
    ) { students, query, filter ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.phone.contains(query)

            val matchesFilter = when (filter) {
                StudentFilter.ALL -> true
                StudentFilter.UNPAID_ONLY -> !student.isFullyPaid
                StudentFilter.PAID_ONLY -> student.isFullyPaid
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Run monthly check and accrue cumulative dues
        viewModelScope.launch {
            repository.checkAndAccrueMonthlyCycles()
            seedSampleDataIfEmpty()
        }

        // Listen to SMS receiver events in real time
        viewModelScope.launch {
            PaymentEventBus.events.collect { event ->
                _activeDraftSms.value = SmsDraftData(
                    studentName = event.student.name,
                    studentPhone = event.student.phone,
                    amountPaid = event.payment.amount,
                    remainingDue = event.student.currentDueBalance,
                    messageText = event.confirmationSms,
                    paymentMethod = event.payment.paymentMethod
                )
                _userMessage.value = "Payment received from ${event.student.name} via ${event.payment.paymentMethod}!"
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: StudentFilter) {
        _selectedFilter.value = filter
    }

    fun openAddStudentDialog() {
        _addEditStudent.value = null
        _showAddEditDialog.value = true
    }

    fun openEditStudentDialog(student: Student) {
        _addEditStudent.value = student
        _showAddEditDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditDialog.value = false
        _addEditStudent.value = null
    }

    fun openRecordPaymentDialog(student: Student) {
        _paymentTargetStudent.value = student
    }

    fun closeRecordPaymentDialog() {
        _paymentTargetStudent.value = null
    }

    fun openStudentDetails(student: Student) {
        _selectedStudentDetails.value = student
    }

    fun closeStudentDetails() {
        _selectedStudentDetails.value = null
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    fun openSmsSimulator() {
        _showSmsSimulator.value = true
    }

    fun closeSmsSimulator() {
        _showSmsSimulator.value = false
    }

    fun openQrPreview() {
        _showQrPreview.value = true
    }

    fun closeQrPreview() {
        _showQrPreview.value = false
    }

    fun dismissDraftSms() {
        _activeDraftSms.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun saveStudent(
        id: Long,
        name: String,
        phone: String,
        monthlyFee: Double,
        dueDayOfMonth: Int,
        currentDueBalance: Double,
        notes: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.addStudent(
                    name = name,
                    phone = phone,
                    monthlyFee = monthlyFee,
                    dueDayOfMonth = dueDayOfMonth,
                    initialDueBalance = currentDueBalance,
                    notes = notes
                )
                _userMessage.value = "Student '$name' added successfully"
            } else {
                val existing = repository.getStudentById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        monthlyFee = monthlyFee,
                        dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31),
                        currentDueBalance = currentDueBalance,
                        notes = notes.trim()
                    )
                    repository.updateStudent(updated)
                    _userMessage.value = "Student '$name' updated"
                }
            }
            closeAddEditDialog()
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (_selectedStudentDetails.value?.id == student.id) {
                _selectedStudentDetails.value = null
            }
            _userMessage.value = "${student.name} deleted"
        }
    }

    fun recordManualPayment(
        studentId: Long,
        amount: Double,
        method: String,
        transactionId: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.recordPayment(
                studentId = studentId,
                amount = amount,
                method = method,
                transactionId = transactionId,
                notes = notes,
                isAutoDetected = false
            )
            closeRecordPaymentDialog()

            if (result != null) {
                val (updatedStudent, payment) = result
                val currentProfile = tutorProfile.value
                val confirmationSms = repository.generateConfirmationSms(
                    studentName = updatedStudent.name,
                    amount = payment.amount,
                    tutorName = currentProfile.tutorName,
                    remainingDue = updatedStudent.currentDueBalance
                )

                // Update selected student details view if open
                if (_selectedStudentDetails.value?.id == updatedStudent.id) {
                    _selectedStudentDetails.value = updatedStudent
                }

                // Show SMS draft confirmation popup
                _activeDraftSms.value = SmsDraftData(
                    studentName = updatedStudent.name,
                    studentPhone = updatedStudent.phone,
                    amountPaid = payment.amount,
                    remainingDue = updatedStudent.currentDueBalance,
                    messageText = confirmationSms,
                    paymentMethod = method
                )
                _userMessage.value = "Payment of ${amount.toInt()} Tk recorded for ${updatedStudent.name}"
            }
        }
    }

    fun addMonthCycle(student: Student) {
        viewModelScope.launch {
            repository.addMonthCycleToStudent(student.id)
            val updated = repository.getStudentById(student.id)
            if (_selectedStudentDetails.value?.id == student.id) {
                _selectedStudentDetails.value = updated
            }
            _userMessage.value = "Added 1 month fee (${student.monthlyFee.toInt()} Tk) to ${student.name}'s balance"
        }
    }

    fun testSimulateSms(sender: String, messageText: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val parsed = SmsParser.parse(sender, messageText)
            if (parsed == null) {
                onResult("Could not parse SMS. Ensure message contains amount (e.g. Tk 2,000) and 11-digit mobile number (e.g. 01712345678).")
                return@launch
            }

            val result = repository.processIncomingSmsPayment(parsed)
            if (result != null) {
                val (student, payment) = result
                val profile = tutorProfile.value
                val confirmationSms = repository.generateConfirmationSms(
                    studentName = student.name,
                    amount = payment.amount,
                    tutorName = profile.tutorName,
                    remainingDue = student.currentDueBalance
                )

                _activeDraftSms.value = SmsDraftData(
                    studentName = student.name,
                    studentPhone = student.phone,
                    amountPaid = payment.amount,
                    remainingDue = student.currentDueBalance,
                    messageText = confirmationSms,
                    paymentMethod = payment.paymentMethod
                )

                onResult("Matched student ${student.name} (${student.phone})!\nPayment of ${payment.amount.toInt()} Tk recorded via ${payment.paymentMethod}.\nRemaining Due: ${student.currentDueBalance.toInt()} Tk.")
            } else {
                onResult("Parsed ${parsed.method} payment of ${parsed.amount.toInt()} Tk from ${parsed.senderPhone}, but no matching student with this mobile number was found in the database.")
            }
        }
    }

    fun updateTutorSettings(name: String, phone: String, autoSend: Boolean) {
        viewModelScope.launch {
            repository.updateTutorProfile(name, phone, autoSend)
            _userMessage.value = "Settings updated"
            closeSettings()
        }
    }

    fun savePickedQrCode(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val targetFile = File(context.filesDir, "tutor_qr_${System.currentTimeMillis()}.png")

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                repository.setQrCodePath(targetFile.absolutePath)
                _userMessage.value = "QR Code saved successfully!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to save QR code: ${e.localizedMessage}"
            }
        }
    }

    fun removeQrCode() {
        viewModelScope.launch {
            repository.clearQrCode()
            _userMessage.value = "QR Code removed"
        }
    }

    private suspend fun seedSampleDataIfEmpty() {
        val count = repository.allStudents.stateIn(viewModelScope).value.size
        if (count == 0) {
            // Seed a few realistic students to give tutor a great first experience
            repository.addStudent(
                name = "Tanvir Ahmed",
                phone = "01711223344",
                monthlyFee = 2000.0,
                dueDayOfMonth = 10,
                initialDueBalance = 4000.0, // 2 months cumulative due
                notes = "HSC Physics batch, 2 months unpaid"
            )

            repository.addStudent(
                name = "Nafis Fuad",
                phone = "01822334455",
                monthlyFee = 2500.0,
                dueDayOfMonth = 5,
                initialDueBalance = 0.0, // Fully paid
                notes = "HSC Chemistry, paid this month"
            )

            repository.addStudent(
                name = "Sumaiya Akter",
                phone = "01933445566",
                monthlyFee = 1800.0,
                dueDayOfMonth = 15,
                initialDueBalance = 900.0, // Partial payment
                notes = "SSC Math batch, half fee paid"
            )

            repository.addStudent(
                name = "Arafat Hossain",
                phone = "01644556677",
                monthlyFee = 3000.0,
                dueDayOfMonth = 7,
                initialDueBalance = 6000.0, // 2 months overdue
                notes = "O Level Physics, overdue"
            )
        }
    }
}

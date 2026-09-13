package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TutorProfile
import com.example.data.model.TutorSettingsManager
import com.example.util.ParsedSmsPayment
import com.example.util.PhoneUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TutorRepository(
    private val database: AppDatabase,
    private val settingsManager: TutorSettingsManager
) {
    private val studentDao = database.studentDao()
    private val paymentDao = database.paymentDao()

    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val tutorProfile: Flow<TutorProfile> = settingsManager.profileFlow

    fun getPaymentsForStudent(studentId: Long): Flow<List<Payment>> {
        return paymentDao.getPaymentsForStudent(studentId)
    }

    suspend fun getStudentById(id: Long): Student? {
        return studentDao.getStudentById(id)
    }

    suspend fun addStudent(
        name: String,
        phone: String,
        monthlyFee: Double,
        dueDayOfMonth: Int,
        initialDueBalance: Double,
        notes: String
    ): Long {
        val currentYearMonth = getCurrentYearMonth()
        val student = Student(
            name = name.trim(),
            phone = phone.trim(),
            monthlyFee = monthlyFee,
            dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31),
            currentDueBalance = initialDueBalance,
            lastBilledYearMonth = currentYearMonth,
            notes = notes.trim()
        )
        return studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    /**
     * Records a payment and deducts the amount from student's current due balance.
     */
    suspend fun recordPayment(
        studentId: Long,
        amount: Double,
        method: String,
        transactionId: String = "",
        senderPhone: String = "",
        notes: String = "",
        isAutoDetected: Boolean = false
    ): Pair<Student, Payment>? {
        val student = studentDao.getStudentById(studentId) ?: return null

        val newBalance = student.currentDueBalance - amount
        val updatedStudent = student.copy(currentDueBalance = newBalance)
        studentDao.updateStudent(updatedStudent)

        val payment = Payment(
            studentId = studentId,
            amount = amount,
            paymentDate = System.currentTimeMillis(),
            paymentMethod = method,
            transactionId = transactionId,
            senderPhone = senderPhone,
            notes = notes,
            isAutoDetected = isAutoDetected
        )
        val paymentId = paymentDao.insertPayment(payment)
        return Pair(updatedStudent, payment.copy(id = paymentId))
    }

    /**
     * Matches an incoming parsed SMS payment with a student in the database.
     * If matched, automatically records payment and deducts from due balance.
     */
    suspend fun processIncomingSmsPayment(parsedSms: ParsedSmsPayment): Pair<Student, Payment>? {
        val students = studentDao.getAllStudentsSnapshot()
        val matchedStudent = students.firstOrNull { student ->
            PhoneUtils.matches(student.phone, parsedSms.senderPhone)
        } ?: return null

        return recordPayment(
            studentId = matchedStudent.id,
            amount = parsedSms.amount,
            method = parsedSms.method,
            transactionId = parsedSms.transactionId,
            senderPhone = parsedSms.senderPhone,
            notes = "Auto-detected via SMS from ${parsedSms.rawSender}",
            isAutoDetected = true
        )
    }

    /**
     * Automatically calculates and adds cumulative due for elapsed monthly cycles.
     * E.g. If unpaid for 2 months at 2000/month, total due becomes 4000.
     */
    suspend fun checkAndAccrueMonthlyCycles() {
        val currentYearMonth = getCurrentYearMonth()
        val students = studentDao.getAllStudentsSnapshot()

        for (student in students) {
            val lastBilled = student.lastBilledYearMonth
            if (lastBilled.isBlank()) {
                // Initialize to current month
                studentDao.updateStudent(student.copy(lastBilledYearMonth = currentYearMonth))
            } else if (lastBilled != currentYearMonth) {
                val elapsedMonths = calculateMonthsBetween(lastBilled, currentYearMonth)
                if (elapsedMonths > 0) {
                    val additionalDue = elapsedMonths * student.monthlyFee
                    val newBalance = student.currentDueBalance + additionalDue
                    studentDao.updateStudent(
                        student.copy(
                            currentDueBalance = newBalance,
                            lastBilledYearMonth = currentYearMonth
                        )
                    )
                }
            }
        }
    }

    /**
     * Manually advances/adds a monthly fee cycle for a student.
     */
    suspend fun addMonthCycleToStudent(studentId: Long) {
        val student = studentDao.getStudentById(studentId) ?: return
        val newBalance = student.currentDueBalance + student.monthlyFee
        studentDao.updateStudent(student.copy(currentDueBalance = newBalance))
    }

    /**
     * Generates confirmation SMS message matching exact requirement:
     * "Dear [Student Name], thank you for your payment of [Amount] Tk to [Tutor Name]. Remaining Due: [Due Amount] Tk."
     */
    fun generateConfirmationSms(
        studentName: String,
        amount: Double,
        tutorName: String,
        remainingDue: Double
    ): String {
        val amtStr = formatCurrency(amount)
        val dueStr = formatCurrency(if (remainingDue <= 0.0) 0.0 else remainingDue)
        val name = tutorName.ifBlank { "Tutor" }
        return "Dear $studentName, thank you for your payment of $amtStr Tk to $name. Remaining Due: $dueStr Tk."
    }

    fun getTutorProfile(): TutorProfile {
        return settingsManager.getProfile()
    }

    fun updateTutorProfile(tutorName: String, tutorPhone: String, autoSendSms: Boolean) {
        settingsManager.updateProfile(tutorName, tutorPhone, autoSendSms)
    }

    fun setQrCodePath(path: String) {
        settingsManager.setQrCodePath(path)
    }

    fun clearQrCode() {
        settingsManager.clearQrCode()
    }

    private fun getCurrentYearMonth(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format("%04d-%02d", year, month)
    }

    private fun calculateMonthsBetween(fromYearMonth: String, toYearMonth: String): Int {
        try {
            val fromParts = fromYearMonth.split("-").map { it.toInt() }
            val toParts = toYearMonth.split("-").map { it.toInt() }
            val fromYear = fromParts[0]
            val fromMonth = fromParts[1]
            val toYear = toParts[0]
            val toMonth = toParts[1]

            return (toYear - fromYear) * 12 + (toMonth - fromMonth)
        } catch (_: Exception) {
            return 0
        }
    }

    private fun formatCurrency(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            String.format("%.2f", amount)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TutorRepository? = null

        fun getInstance(context: Context): TutorRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val settings = TutorSettingsManager.getInstance(context)
                val instance = TutorRepository(db, settings)
                INSTANCE = instance
                instance
            }
        }
    }
}

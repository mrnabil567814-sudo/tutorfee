package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val monthlyFee: Double,
    val dueDayOfMonth: Int = 10,
    val currentDueBalance: Double = 0.0,
    val lastBilledYearMonth: String = "", // e.g., "2026-09"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Determines real-time payment status based on currentDueBalance and due day.
     */
    fun computeStatus(currentDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)): PaymentStatus {
        return when {
            currentDueBalance <= 0.0 -> PaymentStatus.PAID
            currentDueBalance < monthlyFee -> PaymentStatus.PARTIAL
            currentDayOfMonth > dueDayOfMonth -> PaymentStatus.OVERDUE
            else -> PaymentStatus.UNPAID
        }
    }

    /**
     * Returns true if status is fully paid (for green styling).
     */
    val isFullyPaid: Boolean
        get() = currentDueBalance <= 0.0
}

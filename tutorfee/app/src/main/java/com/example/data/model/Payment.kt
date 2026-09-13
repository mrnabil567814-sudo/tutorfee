package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String, // "bKash", "Nagad", "Cash"
    val transactionId: String = "",
    val senderPhone: String = "",
    val notes: String = "",
    val isAutoDetected: Boolean = false
)

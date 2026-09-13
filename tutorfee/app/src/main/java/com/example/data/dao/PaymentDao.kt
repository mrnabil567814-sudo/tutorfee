package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentDate >= :sinceTimestamp")
    suspend fun getPaymentsSince(sinceTimestamp: Long): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Delete
    suspend fun deletePayment(payment: Payment)
}

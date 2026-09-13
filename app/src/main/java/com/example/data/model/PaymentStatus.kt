package com.example.data.model

enum class PaymentStatus {
    PAID,       // Fully paid: currentDueBalance <= 0
    PARTIAL,    // Partially paid: 0 < currentDueBalance < monthlyFee
    UNPAID,     // Unpaid current cycle: currentDueBalance >= monthlyFee
    OVERDUE     // Due date passed and balance > 0
}

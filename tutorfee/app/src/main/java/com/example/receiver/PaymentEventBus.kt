package com.example.receiver

import com.example.data.model.Payment
import com.example.data.model.Student
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AutoPaymentEvent(
    val student: Student,
    val payment: Payment,
    val confirmationSms: String
)

object PaymentEventBus {
    private val _events = MutableSharedFlow<AutoPaymentEvent>(extraBufferCapacity = 5)
    val events: SharedFlow<AutoPaymentEvent> = _events.asSharedFlow()

    fun emitEvent(event: AutoPaymentEvent) {
        _events.tryEmit(event)
    }
}

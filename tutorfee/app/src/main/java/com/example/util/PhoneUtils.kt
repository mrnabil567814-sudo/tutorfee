package com.example.util

object PhoneUtils {
    fun normalize(raw: String?): String = normalizePhoneNumber(raw)

    /**
     * Normalizes a phone number to standard 11 digits (e.g., 01712345678).
     * Handles +88017..., 88017..., spaces, dashes, etc.
     */
    fun normalizePhoneNumber(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("880") && digits.length >= 13 -> digits.substring(2)
            digits.startsWith("01") && digits.length == 11 -> digits
            digits.startsWith("1") && digits.length == 10 -> "0$digits"
            digits.length > 11 -> digits.takeLast(11)
            else -> digits
        }
    }

    /**
     * Checks if two phone numbers match (by comparing their normalized 11-digit forms).
     */
    fun matches(phone1: String?, phone2: String?): Boolean {
        val norm1 = normalizePhoneNumber(phone1)
        val norm2 = normalizePhoneNumber(phone2)
        if (norm1.isBlank() || norm2.isBlank()) return false
        return norm1 == norm2
    }

    /**
     * Formats an 11-digit phone number for display, e.g., 01712-345678.
     */
    fun formatDisplay(phone: String): String {
        val norm = normalizePhoneNumber(phone)
        return if (norm.length == 11) {
            "${norm.substring(0, 5)}-${norm.substring(5)}"
        } else {
            phone
        }
    }
}

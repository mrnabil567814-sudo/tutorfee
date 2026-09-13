package com.example.util

data class ParsedSmsPayment(
    val amount: Double,
    val senderPhone: String,
    val method: String, // "bKash" or "Nagad"
    val transactionId: String,
    val rawSender: String,
    val rawMessage: String
)

object SmsParser {

    /**
     * Checks if the incoming SMS is from bKash or Nagad.
     */
    fun isPaymentSms(sender: String?, messageBody: String?): Boolean {
        return isBkash(sender, messageBody) || isNagad(sender, messageBody)
    }

    fun isBkash(sender: String?, messageBody: String?): Boolean {
        val s = sender ?: ""
        val b = messageBody ?: ""
        return s.contains("bKash", ignoreCase = true) ||
                b.contains("bKash", ignoreCase = true)
    }

    fun isNagad(sender: String?, messageBody: String?): Boolean {
        val s = sender ?: ""
        val b = messageBody ?: ""
        return s.contains("16167") ||
                s.contains("Nagad", ignoreCase = true) ||
                b.contains("Nagad", ignoreCase = true)
    }

    /**
     * Parses the SMS message body and sender into a ParsedSmsPayment object.
     * Returns null if it cannot extract a valid amount or sender phone number.
     */
    fun parse(sender: String?, messageBody: String?): ParsedSmsPayment? {
        if (messageBody.isNullOrBlank()) return null

        val method = when {
            isBkash(sender, messageBody) -> "bKash"
            isNagad(sender, messageBody) -> "Nagad"
            else -> "MFS"
        }

        val amount = extractAmount(messageBody) ?: return null
        val senderPhone = extractSenderPhone(messageBody) ?: return null
        val trxId = extractTransactionId(messageBody) ?: ""

        return ParsedSmsPayment(
            amount = amount,
            senderPhone = PhoneUtils.normalizePhoneNumber(senderPhone),
            method = method,
            transactionId = trxId,
            rawSender = sender ?: "",
            rawMessage = messageBody
        )
    }

    /**
     * Extracts the payment amount from the SMS body.
     */
    private fun extractAmount(text: String): Double? {
        // Pattern 1: Tk/TK/BDT 2,000.00 or Tk. 2000
        val patterns = listOf(
            Regex("""(?:Tk|TK|BDT|৳)\.?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
            Regex("""Amount[:\s]+(?:Tk|TK)?\.?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
            Regex("""(?:received|of)\s+(?:Tk|TK)?\.?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val rawNumber = match.groupValues[1].replace(",", "").trim()
                val parsed = rawNumber.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    return parsed
                }
            }
        }
        return null
    }

    /**
     * Extracts the sender's 11-digit Bangladeshi mobile number from the SMS body.
     */
    private fun extractSenderPhone(text: String): String? {
        // Look specifically after "from", "sender", etc.
        val specificPatterns = listOf(
            Regex("""(?:from|sender|from:)\s*(?:mobile|no)?\.?\s*(?:\+?88)?(01[3-9][0-9]{8})""", RegexOption.IGNORE_CASE),
            Regex("""(?:from|sender)[:\s]+(?:\+?88)?([0-9]{11})""", RegexOption.IGNORE_CASE)
        )

        for (pattern in specificPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        // Fallback: match any standalone 11-digit Bangladeshi mobile number
        val generalPattern = Regex("""\b(01[3-9][0-9]{8})\b""")
        val match = generalPattern.find(text)
        return match?.groupValues?.get(1)
    }

    /**
     * Extracts transaction ID (TrxID or TxnID).
     */
    private fun extractTransactionId(text: String): String? {
        val pattern = Regex("""(?:TrxID|TxnID|TxID|Trx ID|Txn ID)[:\s]+([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.get(1)
    }
}

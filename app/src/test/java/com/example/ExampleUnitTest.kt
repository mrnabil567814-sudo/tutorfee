package com.example

import com.example.util.PhoneUtils
import com.example.util.SmsParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testPhoneNormalizationAndMatching() {
    val phone1 = "01711223344"
    val phone2 = "+8801711223344"
    val phone3 = "8801711-223344"
    val phone4 = "01711 223 344"

    assertEquals("01711223344", PhoneUtils.normalize(phone1))
    assertEquals("01711223344", PhoneUtils.normalize(phone2))
    assertEquals("01711223344", PhoneUtils.normalize(phone3))
    assertEquals("01711223344", PhoneUtils.normalize(phone4))

    assertTrue(PhoneUtils.matches(phone1, phone2))
    assertTrue(PhoneUtils.matches(phone3, phone4))
    assertFalse(PhoneUtils.matches(phone1, "01811223344"))
  }

  @Test
  fun testBkashSmsParsing() {
    val body = "You have received Tk 2,000.00 from 01711223344. Ref Fee Tk 0.00. Balance Tk 15,250.00. TrxID 9H82KA71 at 12/09/2026 14:30"
    val parsed = SmsParser.parse("bKash", body)

    assertNotNull(parsed)
    assertEquals(2000.0, parsed!!.amount, 0.01)
    assertEquals("01711223344", parsed.senderPhone)
    assertEquals("bKash", parsed.method)
    assertEquals("9H82KA71", parsed.transactionId)
  }

  @Test
  fun testNagadSmsParsing() {
    val body = "Cash In of Tk 2,500.00 from 01822334455 successful. Ref: Tuition TxnID: 76HJK29 Balance: Tk 12,000.00."
    val parsed = SmsParser.parse("16167", body)

    assertNotNull(parsed)
    assertEquals(2500.0, parsed!!.amount, 0.01)
    assertEquals("01822334455", parsed.senderPhone)
    assertEquals("Nagad", parsed.method)
    assertEquals("76HJK29", parsed.transactionId)
  }
}


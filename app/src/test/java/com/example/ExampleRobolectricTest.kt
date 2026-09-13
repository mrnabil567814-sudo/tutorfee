package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PaymentStatus
import com.example.data.model.Student
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TutorFee", appName)
  }

  @Test
  fun `test student due and payment status logic`() {
    val paidStudent = Student(
      id = 1,
      name = "Nafis",
      phone = "01822334455",
      monthlyFee = 2500.0,
      dueDayOfMonth = 10,
      currentDueBalance = 0.0
    )
    assertEquals(PaymentStatus.PAID, paidStudent.computeStatus())

    val partialStudent = Student(
      id = 2,
      name = "Sumaiya",
      phone = "01933445566",
      monthlyFee = 2000.0,
      dueDayOfMonth = 10,
      currentDueBalance = 1000.0
    )
    assertEquals(PaymentStatus.PARTIAL, partialStudent.computeStatus())

    val overdueStudent = Student(
      id = 3,
      name = "Tanvir",
      phone = "01711223344",
      monthlyFee = 2000.0,
      dueDayOfMonth = 10,
      currentDueBalance = 4000.0
    )
    assertEquals(PaymentStatus.OVERDUE, overdueStudent.computeStatus())
  }
}


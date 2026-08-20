package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.WarrantyStatus
import com.example.domain.parser.ReceiptParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BillVault", appName)
  }

  @Test
  fun `test receipt parsing on sample apple receipt`() {
    val rawText = """
        APPLE STORE #R214
        5th Avenue, New York, NY
        Date: 10/14/2025
        --------------------------------
        1x MacBook Pro 14" M3 Pro 512GB   $1,999.00
        1x AppleCare+ 3-Year Protection     $279.00
        --------------------------------
        TOTAL: $2,480.17
        3 YEAR LIMITED WARRANTY INCLUDED
    """.trimIndent()

    val parsed = ReceiptParser.parse(rawText)
    assertEquals("Apple Store", parsed.merchantName)
    assertNotNull(parsed.totalAmount)
    assertEquals(2480.17, parsed.totalAmount!!, 0.01)
    assertEquals(36, parsed.warrantyMonths)
    assertEquals(ReceiptCategory.ELECTRONICS, parsed.category)
    assertTrue(parsed.overallConfidence >= 0.8f)
  }

  @Test
  fun `test warranty status calculation`() {
    val now = System.currentTimeMillis()
    val futureDate = now + (1000L * 60 * 60 * 24 * 100) // 100 days
    val expiringSoonDate = now + (1000L * 60 * 60 * 24 * 10) // 10 days
    val pastDate = now - (1000L * 60 * 60 * 24 * 5) // 5 days ago

    assertEquals(WarrantyStatus.ACTIVE, WarrantyStatus.calculate(futureDate, now))
    assertEquals(WarrantyStatus.EXPIRING_SOON, WarrantyStatus.calculate(expiringSoonDate, now))
    assertEquals(WarrantyStatus.EXPIRED, WarrantyStatus.calculate(pastDate, now))
  }
}

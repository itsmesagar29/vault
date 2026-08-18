package com.example

import com.example.domain.model.BrandSupportDirectory
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem
import com.example.domain.model.WarrantyStatus
import com.example.domain.parser.ReceiptParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ReceiptParserTest {

    @Test
    fun parseReceipt_extractsMerchantAndTotal_accurately() {
        val sampleText = """
            RELIANCE DIGITAL
            Store #402, Phoenix Marketcity
            Date: 15/04/2024
            
            1x Sony WH-1000XM5 Headphones   ₹29,990.00
            
            Subtotal: ₹29,990.00
            GST 18%:   ₹5,398.20
            GRAND TOTAL: ₹29,990.00
            
            Includes 2 Years Manufacturer Warranty
            Thank you for shopping with us!
        """.trimIndent()

        val parsed = ReceiptParser.parse(sampleText)

        assertEquals("Reliance Digital", parsed.merchantName)
        assertEquals(29990.00, parsed.totalAmount, 0.01)
        assertEquals("₹", parsed.currency)
        assertEquals(24, parsed.warrantyMonths)
        assertEquals(ReceiptCategory.ELECTRONICS, parsed.category)
    }

    @Test
    fun parseReceipt_extractsCromaElectronics_withOneYearWarranty() {
        val sampleText = """
            CROMA MEGA STORE
            Invoice No: CR-98124
            Date: 2025-01-10
            
            Apple MacBook Air M3
            Amount: 99900.00
            1 Year Standard Apple Guarantee
            
            Total Amount: ₹99,900.00
        """.trimIndent()

        val parsed = ReceiptParser.parse(sampleText)

        assertEquals("Croma", parsed.merchantName)
        assertEquals(99900.00, parsed.totalAmount, 0.01)
        assertEquals(12, parsed.warrantyMonths)
    }

    @Test
    fun warrantyStatus_calculatesCorrectly() {
        val now = System.currentTimeMillis()

        // Expired (10 days in the past)
        val expiredMillis = now - TimeUnit.DAYS.toMillis(10)
        assertEquals(WarrantyStatus.EXPIRED, WarrantyStatus.calculate(expiredMillis))

        // Expiring Soon (15 days in future)
        val expiringSoonMillis = now + TimeUnit.DAYS.toMillis(15)
        assertEquals(WarrantyStatus.EXPIRING_SOON, WarrantyStatus.calculate(expiringSoonMillis))

        // Active (180 days in future)
        val activeMillis = now + TimeUnit.DAYS.toMillis(180)
        assertEquals(WarrantyStatus.ACTIVE, WarrantyStatus.calculate(activeMillis))
    }

    @Test
    fun brandSupportDirectory_findsKnownBrands() {
        val appleSupport = BrandSupportDirectory.findSupportFor("Apple Store Mumbai", "MacBook Pro M3")
        assertNotNull(appleSupport)
        assertEquals("Apple", appleSupport?.brandName)

        val sonySupport = BrandSupportDirectory.findSupportFor("Sony Center", "Bravia 65 Inch TV")
        assertNotNull(sonySupport)
        assertEquals("Sony", sonySupport?.brandName)
    }
}

package com.example

import com.example.data.exporter.ExportHelper
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportHelperTest {

    @Test
    fun generateCsv_containsValidHeadersAndData() {
        val item = ReceiptItem(
            id = 1,
            merchantName = "Reliance Digital",
            itemName = "Sony Headphones",
            totalAmount = 24999.0,
            currency = "₹",
            purchaseDateMillis = 1713139200000L,
            warrantyMonths = 24,
            warrantyExpiryDateMillis = 1776211200000L,
            category = ReceiptCategory.ELECTRONICS,
            notes = "Test notes"
        )

        val csv = ExportHelper.generateCsv(listOf(item))

        assertTrue(csv.contains("Merchant,Item,Amount"))
        assertTrue(csv.contains("Reliance Digital"))
        assertTrue(csv.contains("Sony Headphones"))
        assertTrue(csv.contains("24999.0"))
    }

    @Test
    fun generateJson_containsValidFields() {
        val item = ReceiptItem(
            id = 1,
            merchantName = "Apple Store",
            itemName = "iPhone 15",
            totalAmount = 79900.0,
            currency = "₹",
            purchaseDateMillis = 1713139200000L,
            warrantyMonths = 12,
            warrantyExpiryDateMillis = 1744675200000L,
            category = ReceiptCategory.ELECTRONICS
        )

        val json = ExportHelper.generateJson(listOf(item))

        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
        assertTrue(json.contains("\"merchantName\": \"Apple Store\""))
        assertTrue(json.contains("\"totalAmount\": 79900.0"))
    }

    @Test
    fun generateWarrantyClaimEmail_createsFormalTemplate() {
        val item = ReceiptItem(
            id = 42,
            merchantName = "Dyson India",
            itemName = "V12 Detect Slim Vacuum",
            totalAmount = 54900.0,
            currency = "₹",
            purchaseDateMillis = 1713139200000L,
            warrantyMonths = 24,
            warrantyExpiryDateMillis = 1776211200000L,
            category = ReceiptCategory.APPLIANCES,
            notes = "Motor making high pitched whistling noise."
        )

        val (subject, body) = ExportHelper.generateWarrantyClaimEmail(item, "ask@dyson.in")

        assertTrue(subject.contains("Warranty Service Claim"))
        assertTrue(subject.contains("Dyson India"))
        assertTrue(body.contains("V12 Detect Slim Vacuum"))
        assertTrue(body.contains("Registered via BillVault"))
    }
}

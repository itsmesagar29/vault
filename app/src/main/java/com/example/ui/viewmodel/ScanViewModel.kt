package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.model.ParsedReceipt
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem
import com.example.domain.parser.ReceiptOcrEngine
import com.example.domain.parser.ReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ScanStatus {
    object Idle : ScanStatus
    object Processing : ScanStatus
    data class Success(val parsedReceipt: ParsedReceipt) : ScanStatus
    data class Error(val message: String) : ScanStatus
}

data class ReviewFormState(
    val merchantName: String = "",
    val itemName: String = "",
    val totalAmount: String = "",
    val currency: String = "$",
    val purchaseDateMillis: Long = System.currentTimeMillis(),
    val warrantyMonths: Int = 12,
    val warrantyExpiryDateMillis: Long = System.currentTimeMillis(),
    val category: ReceiptCategory = ReceiptCategory.ELECTRONICS,
    val notes: String = "",
    val imagePath: String? = null,
    val rawOcrText: String = "",
    val overallConfidence: Float = 1.0f,
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 7
)

data class SampleReceiptPreset(
    val title: String,
    val rawText: String
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val ocrEngine = ReceiptOcrEngine(application)
    private val userPrefs = UserPreferencesRepository(application)

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus

    private val _reviewForm = MutableStateFlow(ReviewFormState())
    val reviewForm: StateFlow<ReviewFormState> = _reviewForm

    val samplePresets = listOf(
        SampleReceiptPreset(
            title = "Apple Store (MacBook Pro)",
            rawText = """
                APPLE STORE #R214
                5th Avenue, New York, NY
                Date: 10/14/2025
                --------------------------------
                1x MacBook Pro 14" M3 Pro 512GB   $1,999.00
                1x AppleCare+ 3-Year Protection     $279.00
                --------------------------------
                SUBTOTAL:                        $2,278.00
                TAX (8.875%):                      $202.17
                TOTAL:                           $2,480.17
                3 YEAR LIMITED WARRANTY INCLUDED
                THANK YOU FOR CHOOSING APPLE
            """.trimIndent()
        ),
        SampleReceiptPreset(
            title = "Best Buy (LG OLED 4K TV)",
            rawText = """
                BEST BUY STORE #0981
                SAN FRANCISCO, CA
                PURCHASE DATE: 11/28/2025
                ------------------------------------
                LG OLED 65" EVO C4 SERIES 4K TV
                SKU: 6583921
                PRICE: $1,696.99
                ------------------------------------
                TOTAL AMOUNT: $1,696.99
                2-YEAR MANUFACTURER WARRANTY
                GEEK SQUAD PROTECTION ELIGIBLE
                RETAIN FOR WARRANTY CLAIMS
            """.trimIndent()
        ),
        SampleReceiptPreset(
            title = "The Home Depot (DeWalt Combo)",
            rawText = """
                THE HOME DEPOT #1042
                CHICAGO, IL
                DATE: 01/15/2026
                ------------------------------------
                DEWALT 20V MAX 2-TOOL DRILL KIT
                MODEL: DCK280C2
                PRICE: $229.00
                ------------------------------------
                TOTAL: $229.00
                3 YEAR LIMITED WARRANTY
                FREE 1 YEAR SERVICE CONTRACT
                REGISTER AT WWW.DEWALT.COM
            """.trimIndent()
        ),
        SampleReceiptPreset(
            title = "IKEA (Ergonomic Chair)",
            rawText = """
                IKEA EMERYVILLE
                DATE: 04/05/2025
                ------------------------------------
                MARKUS OFFICE CHAIR BLACK
                ART.NO: 702.611.50
                AMOUNT: $289.99
                ------------------------------------
                TOTAL: $289.99
                10-YEAR GUARANTEE INCLUDED
                KEEP RECEIPT AS PROOF OF PURCHASE
            """.trimIndent()
        )
    )

    fun processBitmap(bitmap: Bitmap) {
        _scanStatus.value = ScanStatus.Processing
        viewModelScope.launch {
            val defaultWarranty = userPrefs.defaultWarrantyMonths.first()
            val savedPath = ocrEngine.saveBitmapToInternalStorage(bitmap)
            val result = ocrEngine.recognizeAndParse(bitmap, defaultWarranty)
            
            result.onSuccess { parsed ->
                _scanStatus.value = ScanStatus.Success(parsed)
                populateReviewForm(parsed, savedPath)
            }.onFailure { error ->
                _scanStatus.value = ScanStatus.Error(error.message ?: "OCR Recognition Failed")
            }
        }
    }

    fun processUri(uri: Uri) {
        _scanStatus.value = ScanStatus.Processing
        viewModelScope.launch {
            val defaultWarranty = userPrefs.defaultWarrantyMonths.first()
            val result = ocrEngine.recognizeAndParse(uri, defaultWarranty)
            
            result.onSuccess { parsed ->
                _scanStatus.value = ScanStatus.Success(parsed)
                populateReviewForm(parsed, uri.toString())
            }.onFailure { error ->
                _scanStatus.value = ScanStatus.Error(error.message ?: "Failed to read image")
            }
        }
    }

    fun processSampleText(rawText: String) {
        _scanStatus.value = ScanStatus.Processing
        viewModelScope.launch {
            val defaultWarranty = userPrefs.defaultWarrantyMonths.first()
            val parsed = ocrEngine.parseFromText(rawText, defaultWarranty)
            _scanStatus.value = ScanStatus.Success(parsed)
            populateReviewForm(parsed, null)
        }
    }

    private fun populateReviewForm(parsed: ParsedReceipt, imagePath: String?) {
        val currency = parsed.currency
        val amountStr = parsed.totalAmount?.let { String.format("%.2f", it) } ?: ""
        
        _reviewForm.value = ReviewFormState(
            merchantName = parsed.merchantName,
            itemName = parsed.itemName,
            totalAmount = amountStr,
            currency = currency,
            purchaseDateMillis = parsed.purchaseDateMillis,
            warrantyMonths = parsed.warrantyMonths,
            warrantyExpiryDateMillis = parsed.warrantyExpiryDateMillis,
            category = parsed.category,
            notes = "",
            imagePath = imagePath,
            rawOcrText = parsed.rawText,
            overallConfidence = parsed.overallConfidence,
            reminderEnabled = true,
            reminderDaysBefore = 7
        )
    }

    fun prepareEditExisting(receipt: ReceiptItem) {
        _reviewForm.value = ReviewFormState(
            merchantName = receipt.merchantName,
            itemName = receipt.itemName,
            totalAmount = String.format("%.2f", receipt.totalAmount),
            currency = receipt.currency,
            purchaseDateMillis = receipt.purchaseDateMillis,
            warrantyMonths = receipt.warrantyMonths,
            warrantyExpiryDateMillis = receipt.warrantyExpiryDateMillis,
            category = receipt.category,
            notes = receipt.notes,
            imagePath = receipt.imagePath,
            rawOcrText = receipt.rawOcrText,
            overallConfidence = receipt.confidenceScore,
            reminderEnabled = receipt.reminderEnabled,
            reminderDaysBefore = receipt.reminderDaysBefore
        )
    }

    fun updateMerchantName(name: String) {
        _reviewForm.value = _reviewForm.value.copy(merchantName = name)
    }

    fun updateItemName(name: String) {
        _reviewForm.value = _reviewForm.value.copy(itemName = name)
    }

    fun updateTotalAmount(amount: String) {
        _reviewForm.value = _reviewForm.value.copy(totalAmount = amount)
    }

    fun updateCurrency(currency: String) {
        _reviewForm.value = _reviewForm.value.copy(currency = currency)
    }

    fun updateCategory(category: ReceiptCategory) {
        _reviewForm.value = _reviewForm.value.copy(category = category)
    }

    fun updatePurchaseDate(dateMillis: Long) {
        val newExpiry = ReceiptParser.calculateExpiryDate(dateMillis, _reviewForm.value.warrantyMonths)
        _reviewForm.value = _reviewForm.value.copy(
            purchaseDateMillis = dateMillis,
            warrantyExpiryDateMillis = newExpiry
        )
    }

    fun updateWarrantyMonths(months: Int) {
        val newExpiry = ReceiptParser.calculateExpiryDate(_reviewForm.value.purchaseDateMillis, months)
        _reviewForm.value = _reviewForm.value.copy(
            warrantyMonths = months,
            warrantyExpiryDateMillis = newExpiry
        )
    }

    fun updateNotes(notes: String) {
        _reviewForm.value = _reviewForm.value.copy(notes = notes)
    }

    fun updateReminderEnabled(enabled: Boolean) {
        _reviewForm.value = _reviewForm.value.copy(reminderEnabled = enabled)
    }

    fun updateReminderDaysBefore(days: Int) {
        _reviewForm.value = _reviewForm.value.copy(reminderDaysBefore = days)
    }

    fun buildReceiptItem(existingId: Long = 0): ReceiptItem {
        val form = _reviewForm.value
        val amount = form.totalAmount.toDoubleOrNull() ?: 0.0
        return ReceiptItem(
            id = existingId,
            merchantName = form.merchantName.ifBlank { "Unknown Merchant" },
            itemName = form.itemName.ifBlank { "${form.category.displayName} Item" },
            totalAmount = amount,
            currency = form.currency,
            purchaseDateMillis = form.purchaseDateMillis,
            warrantyMonths = form.warrantyMonths,
            warrantyExpiryDateMillis = form.warrantyExpiryDateMillis,
            category = form.category,
            notes = form.notes,
            imagePath = form.imagePath,
            rawOcrText = form.rawOcrText,
            confidenceScore = form.overallConfidence,
            reminderEnabled = form.reminderEnabled,
            reminderDaysBefore = form.reminderDaysBefore,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun resetState() {
        _scanStatus.value = ScanStatus.Idle
        _reviewForm.value = ReviewFormState()
    }
}

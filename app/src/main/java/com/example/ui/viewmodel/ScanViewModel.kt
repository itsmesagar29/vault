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
    val currency: String = "₹",
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
            title = "Apple Saket (iPhone 16 Pro)",
            rawText = """
                APPLE SAKET NEW DELHI
                Select CITYWALK Mall, Saket
                Date: 15/10/2025
                --------------------------------
                1x iPhone 16 Pro 256GB Black     ₹1,29,900.00
                1x AppleCare+ 2-Year Plan          ₹19,900.00
                --------------------------------
                SUBTOTAL:                        ₹1,49,800.00
                GST 18% INCLUDED
                TOTAL:                           ₹1,49,800.00
                2 YEAR COMPREHENSIVE WARRANTY
                THANK YOU FOR CHOOSING APPLE
            """.trimIndent()
        ),
        SampleReceiptPreset(
            title = "Croma (Sony 55\" 4K TV)",
            rawText = """
                CROMA ELECTRONICS #0981
                INDIRANAGAR, BANGALORE
                PURCHASE DATE: 28/11/2025
                ------------------------------------
                SONY BRAVIA 55" 4K GOOGLE TV
                MODEL: KD-55X74L
                PRICE: ₹64,990.00
                ------------------------------------
                TOTAL AMOUNT: ₹64,990.00
                2-YEAR MANUFACTURER WARRANTY
                CROMA SHIELD ELIGIBLE
                RETAIN FOR WARRANTY CLAIMS
            """.trimIndent()
        ),
        SampleReceiptPreset(
            title = "Vijay Sales (Bosch Washing Machine)",
            rawText = """
                VIJAY SALES RETAIL #1042
                ANDHERI WEST, MUMBAI
                DATE: 15/01/2026
                ------------------------------------
                BOSCH 8KG FRONT LOAD WASHER
                MODEL: WAJ2846WIN
                PRICE: ₹36,990.00
                ------------------------------------
                TOTAL: ₹36,990.00
                3 YEAR COMPREHENSIVE WARRANTY
                10 YEAR MOTOR WARRANTY
                REGISTER AT WWW.BOSCH-HOME.IN
            """.trimIndent()
        ),
        SampleReceiptPreset(
            title = "Decathlon (Triban Road Bike)",
            rawText = """
                DECATHLON SPORTS INDIA
                SECTOR 29, GURUGRAM
                DATE: 05/04/2025
                ------------------------------------
                TRIBAN RC100 ROAD BIKE 7-SPEED
                ITEM CODE: 8544956
                AMOUNT: ₹24,999.00
                ------------------------------------
                TOTAL: ₹24,999.00
                LIFETIME FRAME WARRANTY
                2 YEAR PARTS WARRANTY
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

package com.example.domain.parser

import com.example.domain.model.ParsedReceipt
import com.example.domain.model.ReceiptCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object ReceiptParser {

    private val KNOWN_MERCHANTS = listOf(
        "Apple Store", "Apple", "Best Buy", "Target", "Walmart", "Home Depot",
        "Lowe's", "Costco", "Amazon", "IKEA", "B&H Photo", "Micro Center",
        "Samsung", "Sony Store", "AutoZone", "Nike Store", "Zara", "Staples",
        "Office Depot", "Wayfair", "Sephora", "Nordstrom", "Macy's", "Target Store"
    )

    private val DATE_FORMATS = listOf(
        "MM/dd/yyyy",
        "MM-dd-yyyy",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MMM dd, yyyy",
        "MMMM dd, yyyy",
        "dd MMM yyyy",
        "MM/dd/yy",
        "dd/MM/yy"
    )

    fun parse(rawText: String, defaultWarrantyMonths: Int = 12): ParsedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        
        var merchantConfidence = 0.5f
        var amountConfidence = 0.5f
        var dateConfidence = 0.5f
        var warrantyConfidence = 0.5f

        // 1. Merchant Extraction
        val merchant = extractMerchant(lines).also {
            if (it.isNotEmpty()) merchantConfidence = 0.85f
        }

        // 2. Total Amount Extraction
        val (amount, currency) = extractTotalAmount(lines).also { (amt, _) ->
            if (amt != null && amt > 0) amountConfidence = 0.90f
        }

        // 3. Purchase Date Extraction
        val purchaseDate = extractPurchaseDate(lines).also { dateMillis ->
            if (dateMillis > 0) dateConfidence = 0.85f
        }
        val effectivePurchaseDate = if (purchaseDate > 0) purchaseDate else System.currentTimeMillis()

        // 4. Warranty Detection
        val detectedWarrantyMonths = extractWarrantyMonths(rawText)
        val warrantyMonths = if (detectedWarrantyMonths != null) {
            warrantyConfidence = 0.95f
            detectedWarrantyMonths
        } else {
            warrantyConfidence = 0.60f
            val categoryDefault = ReceiptCategory.inferFromText("$merchant $rawText").defaultWarrantyMonths
            if (categoryDefault > 0) categoryDefault else defaultWarrantyMonths
        }

        // 5. Item Name / Description
        val itemName = extractItemName(lines, merchant)

        // 6. Category Inference
        val category = ReceiptCategory.inferFromText("$merchant $itemName $rawText")

        // 7. Calculate Expiry Date
        val expiryDate = calculateExpiryDate(effectivePurchaseDate, warrantyMonths)

        // 8. Overall Confidence Score
        val overallConfidence = (merchantConfidence * 0.3f) +
                (amountConfidence * 0.35f) +
                (dateConfidence * 0.2f) +
                (warrantyConfidence * 0.15f)

        val fieldConfidenceMap = mapOf(
            "merchant" to merchantConfidence,
            "amount" to amountConfidence,
            "date" to dateConfidence,
            "warranty" to warrantyConfidence
        )

        return ParsedReceipt(
            merchantName = merchant.ifEmpty { "Receipt Merchant" },
            itemName = itemName.ifEmpty { "${category.displayName} Item" },
            totalAmount = amount,
            currency = currency,
            purchaseDateMillis = effectivePurchaseDate,
            warrantyMonths = warrantyMonths,
            warrantyExpiryDateMillis = expiryDate,
            category = category,
            rawText = rawText,
            overallConfidence = overallConfidence.coerceIn(0.1f, 1.0f),
            fieldConfidence = fieldConfidenceMap,
            extractedLines = lines
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        // Look for known merchants first anywhere in the top 6 lines
        for (i in 0 until minOf(6, lines.size)) {
            val line = lines[i]
            for (known in KNOWN_MERCHANTS) {
                if (line.contains(known, ignoreCase = true)) {
                    return known
                }
            }
        }

        // Fallback: pick the first sensible line (letters, length between 3 and 30, not purely numbers or symbols)
        for (line in lines.take(4)) {
            val clean = line.replace(Regex("[^a-zA-Z0-9 &.'-]"), "").trim()
            if (clean.length in 3..32 && clean.any { it.isLetter() } && !clean.contains("receipt", ignoreCase = true) && !clean.contains("welcome", ignoreCase = true)) {
                return clean
            }
        }

        return lines.firstOrNull()?.take(28) ?: ""
    }

    private fun parseAmountString(rawStr: String): Double? {
        val clean = rawStr.trim()
        if (clean.contains(",") && clean.contains(".")) {
            return if (clean.lastIndexOf(".") > clean.lastIndexOf(",")) {
                // US style: 1,999.00 -> 1999.00
                clean.replace(",", "").toDoubleOrNull()
            } else {
                // EU style: 1.999,00 -> 1999.00
                clean.replace(".", "").replace(",", ".").toDoubleOrNull()
            }
        } else if (clean.contains(",")) {
            // Check if comma is decimal (e.g. 19,99) or thousands (e.g. 1,000)
            val parts = clean.split(",")
            return if (parts.size == 2 && parts[1].length == 2) {
                clean.replace(",", ".").toDoubleOrNull()
            } else {
                clean.replace(",", "").toDoubleOrNull()
            }
        }
        return clean.toDoubleOrNull()
    }

    private fun extractTotalAmount(lines: List<String>): Pair<Double?, String> {
        val totalKeywords = listOf("GRAND TOTAL", "TOTAL AMOUNT", "TOTAL DUE", "BALANCE DUE", "TOTAL", "AMOUNT DUE", "AMOUNT PAID", "SUBTOTAL")
        val amountPattern = Pattern.compile("""([$€£¥₹]?)\s*([0-9]{1,3}(?:[,\.][0-9]{3})*(?:[,\.][0-9]{2})|[0-9]+[.,][0-9]{2}|[0-9]+)""")

        // Search lines from bottom up or lines matching total keywords
        for (line in lines.reversed()) {
            val upper = line.uppercase()
            for (keyword in totalKeywords) {
                if (upper.contains(keyword)) {
                    val matcher = amountPattern.matcher(line)
                    var lastFoundAmount: Double? = null
                    var lastFoundSymbol = "$"
                    while (matcher.find()) {
                        val symbol = matcher.group(1)?.ifEmpty { "$" } ?: "$"
                        val rawNum = matcher.group(2) ?: ""
                        val num = parseAmountString(rawNum)
                        if (num != null && num > 0) {
                            lastFoundAmount = num
                            lastFoundSymbol = symbol
                        }
                    }
                    if (lastFoundAmount != null) {
                        return Pair(lastFoundAmount, lastFoundSymbol)
                    }
                }
            }
        }

        // Fallback: find the largest dollar amount in the document
        var maxAmount: Double? = null
        var foundCurrency = "$"
        for (line in lines) {
            val matcher = amountPattern.matcher(line)
            while (matcher.find()) {
                val symbol = matcher.group(1)?.ifEmpty { "$" } ?: "$"
                val rawNum = matcher.group(2) ?: ""
                val num = parseAmountString(rawNum)
                if (num != null && (maxAmount == null || num > maxAmount)) {
                    maxAmount = num
                    foundCurrency = symbol
                }
            }
        }

        return Pair(maxAmount, foundCurrency)
    }

    private fun extractPurchaseDate(lines: List<String>): Long {
        // Regex for various date formats
        val dateRegex = Regex("""\b(\d{1,4}[/-]\d{1,2}[/-]\d{1,4}|\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},?\s+\d{2,4})\b""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val match = dateRegex.find(line)
            if (match != null) {
                val dateStr = match.value.trim()
                for (fmt in DATE_FORMATS) {
                    try {
                        val sdf = SimpleDateFormat(fmt, Locale.US)
                        sdf.isLenient = false
                        val parsed = sdf.parse(dateStr)
                        if (parsed != null && isValidDate(parsed)) {
                            return parsed.time
                        }
                    } catch (_: Exception) {
                        // ignore and try next format
                    }
                }
            }
        }
        return 0L
    }

    private fun isValidDate(date: Date): Boolean {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        cal.time = date
        val parsedYear = cal.get(Calendar.YEAR)
        // Valid receipt date should be between 2000 and currentYear + 1
        return parsedYear in 2000..(currentYear + 1)
    }

    private fun extractWarrantyMonths(rawText: String): Int? {
        val lower = rawText.lowercase()

        // 2 year / 3 years / 5 years warranty
        val yearPattern = Regex("""(\d+)\s*(?:year|yr|years|yrs)\s*(?:limited\s*)?(?:warranty|guarantee|protection)""")
        val yearMatch = yearPattern.find(lower)
        if (yearMatch != null) {
            val years = yearMatch.groupValues[1].toIntOrNull()
            if (years != null && years in 1..20) {
                return years * 12
            }
        }

        // Months pattern: 24 months / 6 months warranty
        val monthPattern = Regex("""(\d+)\s*(?:month|mo|months|mos)\s*(?:limited\s*)?(?:warranty|guarantee|protection)""")
        val monthMatch = monthPattern.find(lower)
        if (monthMatch != null) {
            val months = monthMatch.groupValues[1].toIntOrNull()
            if (months != null && months in 1..120) {
                return months
            }
        }

        // Lifetime warranty
        if (lower.contains("lifetime warranty") || lower.contains("lifetime guarantee")) {
            return 120 // 10 years representation
        }

        // 1 Year default keywords
        if (lower.contains("1-year warranty") || lower.contains("1 year warranty") || lower.contains("1 yr warranty")) {
            return 12
        }
        if (lower.contains("2-year warranty") || lower.contains("2 year warranty") || lower.contains("2 yr warranty")) {
            return 24
        }
        if (lower.contains("3-year warranty") || lower.contains("3 year warranty")) {
            return 36
        }

        return null
    }

    private fun extractItemName(lines: List<String>, merchant: String): String {
        // Look for lines that look like purchased products (between merchant header and total)
        val skipKeywords = listOf("subtotal", "tax", "total", "cash", "change", "card", "visa", "mastercard", "date", "time", "order", "receipt", "tel", "phone", "street", "ave", "road", "www", ".com")
        
        for (i in 1 until minOf(lines.size, 10)) {
            val line = lines[i].trim()
            val lower = line.lowercase()
            if (line.equals(merchant, ignoreCase = true)) continue
            if (line.length in 4..40 && skipKeywords.none { lower.contains(it) } && line.any { it.isLetter() }) {
                // remove trailing price if present
                val cleaned = line.replace(Regex("""[$€£¥₹]?\s*\d+[.,]\d{2}.*$"""), "").trim()
                if (cleaned.length >= 3) {
                    return cleaned
                }
            }
        }
        return ""
    }

    fun calculateExpiryDate(purchaseDateMillis: Long, warrantyMonths: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = purchaseDateMillis
        cal.add(Calendar.MONTH, warrantyMonths)
        return cal.timeInMillis
    }
}

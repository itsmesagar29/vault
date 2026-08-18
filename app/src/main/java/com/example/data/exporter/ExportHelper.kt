package com.example.data.exporter

import android.content.Context
import android.content.Intent
import com.example.domain.model.ReceiptItem
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun generateCsv(receipts: List<ReceiptItem>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("ID,Merchant,Item,Amount,Currency,Purchase Date,Warranty (Months),Expiry Date,Status,Category,Notes,Created Date\n")

        for (item in receipts) {
            val status = item.warrantyStatus.name
            val escapeCsv = { text: String ->
                "\"" + text.replace("\"", "\"\"").replace("\n", " ") + "\""
            }

            sb.append("${item.id},")
            sb.append("${escapeCsv(item.merchantName)},")
            sb.append("${escapeCsv(item.itemName)},")
            sb.append("${item.totalAmount},")
            sb.append("${item.currency},")
            sb.append("${item.formattedPurchaseDate},")
            sb.append("${item.warrantyMonths},")
            sb.append("${item.formattedExpiryDate},")
            sb.append("$status,")
            sb.append("${item.category.displayName},")
            sb.append("${escapeCsv(item.notes)},")
            sb.append("${item.formattedPurchaseDate}\n")
        }
        return sb.toString()
    }

    fun generateJson(receipts: List<ReceiptItem>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        receipts.forEachIndexed { index, item ->
            val isLast = index == receipts.size - 1
            sb.append("  {\n")
            sb.append("    \"id\": ${item.id},\n")
            sb.append("    \"merchantName\": \"${escapeJson(item.merchantName)}\",\n")
            sb.append("    \"itemName\": \"${escapeJson(item.itemName)}\",\n")
            sb.append("    \"totalAmount\": ${item.totalAmount},\n")
            sb.append("    \"currency\": \"${escapeJson(item.currency)}\",\n")
            sb.append("    \"purchaseDate\": \"${item.formattedPurchaseDate}\",\n")
            sb.append("    \"purchaseDateMillis\": ${item.purchaseDateMillis},\n")
            sb.append("    \"warrantyMonths\": ${item.warrantyMonths},\n")
            sb.append("    \"warrantyExpiryDate\": \"${item.formattedExpiryDate}\",\n")
            sb.append("    \"warrantyExpiryDateMillis\": ${item.warrantyExpiryDateMillis},\n")
            sb.append("    \"category\": \"${item.category.name}\",\n")
            sb.append("    \"notes\": \"${escapeJson(item.notes)}\",\n")
            sb.append("    \"status\": \"${item.warrantyStatus.name}\"\n")
            sb.append("  }${if (isLast) "" else ","}\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun generateWarrantyClaimEmail(receipt: ReceiptItem, recipientEmail: String?): Pair<String, String> {
        val subject = "Warranty Service Claim: ${receipt.merchantName} - ${receipt.itemName} [Ref: #CLM-${receipt.id}]"
        val body = """
            Dear Customer Support Team,

            I am writing to initiate a warranty service / repair claim for my purchase:

            ■ PRODUCT DETAILS:
            • Product Name: ${receipt.itemName}
            • Merchant / Store: ${receipt.merchantName}
            • Date of Purchase: ${receipt.formattedPurchaseDate}
            • Invoice Amount: ${receipt.formattedAmount}
            • Warranty Duration: ${receipt.warrantyMonths} Months
            • Expiry Date: ${receipt.formattedExpiryDate} (${if (receipt.remainingDays >= 0) "${receipt.remainingDays} days active" else "Expired"})
            • Vault Claim Ref: #CLM-${receipt.id}-${System.currentTimeMillis() % 10000}

            ■ ISSUE SUMMARY & NOTES:
            ${receipt.notes.ifBlank { "Requesting standard inspection and warranty service coverage under manufacturer terms." }}

            Please provide instructions on the nearest authorized service center, pickup schedule, or service case ID.

            Thank you,
            Registered via BillVault
        """.trimIndent()

        return Pair(subject, body)
    }

    fun shareText(context: Context, text: String, title: String = "Share Data") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, title)
        }
        val chooser = Intent.createChooser(sendIntent, title)
        context.startActivity(chooser)
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("\t", "\\t")
    }
}

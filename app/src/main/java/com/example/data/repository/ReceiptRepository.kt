package com.example.data.repository

import android.content.Context
import com.example.data.db.ReceiptDao
import com.example.data.db.ReceiptDatabase
import com.example.data.db.ReceiptEntity
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem
import com.example.domain.model.WarrantyStatus
import com.example.domain.parser.ReceiptParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class VaultStats(
    val totalReceiptsCount: Int = 0,
    val activeWarrantiesCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val totalVaultValue: Double = 0.0,
    val currency: String = "$"
)

class ReceiptRepository(private val context: Context) {
    private val dao: ReceiptDao = ReceiptDatabase.getDatabase(context).receiptDao()

    val allReceipts: Flow<List<ReceiptItem>> = dao.getAllReceipts().map { list ->
        list.map { it.toDomain() }
    }

    val expiringSoonReceipts: Flow<List<ReceiptItem>> = dao.getAllReceipts().map { list ->
        val now = System.currentTimeMillis()
        val thirtyDaysFromNow = now + TimeUnit.DAYS.toMillis(30)
        list.map { it.toDomain() }.filter { item ->
            item.warrantyExpiryDateMillis in now..thirtyDaysFromNow
        }.sortedBy { it.warrantyExpiryDateMillis }
    }

    val activeReceipts: Flow<List<ReceiptItem>> = dao.getAllReceipts().map { list ->
        val now = System.currentTimeMillis()
        list.map { it.toDomain() }.filter { it.warrantyExpiryDateMillis > now }
            .sortedBy { it.warrantyExpiryDateMillis }
    }

    val vaultStats: Flow<VaultStats> = dao.getAllReceipts().map { list ->
        val now = System.currentTimeMillis()
        val thirtyDaysFromNow = now + TimeUnit.DAYS.toMillis(30)
        val domainItems = list.map { it.toDomain() }
        
        var active = 0
        var expiring = 0
        var expired = 0
        var totalValue = 0.0

        for (item in domainItems) {
            totalValue += item.totalAmount
            when {
                item.warrantyExpiryDateMillis <= 0 -> {}
                item.warrantyExpiryDateMillis < now -> expired++
                item.warrantyExpiryDateMillis <= thirtyDaysFromNow -> expiring++
                else -> active++
            }
        }

        VaultStats(
            totalReceiptsCount = domainItems.size,
            activeWarrantiesCount = active,
            expiringSoonCount = expiring,
            expiredCount = expired,
            totalVaultValue = totalValue,
            currency = domainItems.firstOrNull()?.currency ?: "$"
        )
    }

    fun getReceiptById(id: Long): Flow<ReceiptItem?> = dao.getReceiptById(id).map { it?.toDomain() }

    suspend fun getReceiptByIdDirect(id: Long): ReceiptItem? = dao.getReceiptByIdDirect(id)?.toDomain()

    fun searchReceipts(query: String): Flow<List<ReceiptItem>> = dao.searchReceipts(query).map { list ->
        list.map { it.toDomain() }
    }

    fun getReceiptsByCategory(category: ReceiptCategory): Flow<List<ReceiptItem>> =
        dao.getReceiptsByCategory(category.name).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun insertReceipt(item: ReceiptItem): Long {
        val entity = ReceiptEntity.fromDomain(item)
        return dao.insertReceipt(entity)
    }

    suspend fun updateReceipt(item: ReceiptItem) {
        val entity = ReceiptEntity.fromDomain(item.copy(updatedAt = System.currentTimeMillis()))
        dao.updateReceipt(entity)
    }

    suspend fun deleteReceipt(item: ReceiptItem) {
        dao.deleteReceipt(ReceiptEntity.fromDomain(item))
    }

    suspend fun deleteReceiptById(id: Long) {
        dao.deleteReceiptById(id)
    }

    suspend fun clearAll() {
        dao.clearAllReceipts()
    }

    suspend fun seedSampleReceiptsIfEmpty() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // 1. Expiring soon item (purchased 23 months ago, 24 mo warranty -> expires in ~5 days)
        cal.timeInMillis = now
        cal.add(Calendar.MONTH, -23)
        cal.add(Calendar.DAY_OF_YEAR, -25)
        val macbookPurchase = cal.timeInMillis
        val macbookExpiry = ReceiptParser.calculateExpiryDate(macbookPurchase, 24)

        // 2. Active item (purchased 3 months ago, 24 mo warranty)
        cal.timeInMillis = now
        cal.add(Calendar.MONTH, -3)
        val tvPurchase = cal.timeInMillis
        val tvExpiry = ReceiptParser.calculateExpiryDate(tvPurchase, 24)

        // 3. Expiring very soon item (purchased 11 months ago, 12 mo warranty -> expires in 12 days)
        cal.timeInMillis = now
        cal.add(Calendar.MONTH, -11)
        cal.add(Calendar.DAY_OF_YEAR, -18)
        val vacuumPurchase = cal.timeInMillis
        val vacuumExpiry = ReceiptParser.calculateExpiryDate(vacuumPurchase, 12)

        // 4. Expired item (purchased 14 months ago, 12 mo warranty)
        cal.timeInMillis = now
        cal.add(Calendar.MONTH, -14)
        val jacketPurchase = cal.timeInMillis
        val jacketExpiry = ReceiptParser.calculateExpiryDate(jacketPurchase, 3)

        // 5. Active item (purchased 1 month ago, 36 mo warranty)
        cal.timeInMillis = now
        cal.add(Calendar.MONTH, -1)
        val drillPurchase = cal.timeInMillis
        val drillExpiry = ReceiptParser.calculateExpiryDate(drillPurchase, 36)

        val samples = listOf(
            ReceiptEntity(
                merchantName = "Apple Store",
                itemName = "MacBook Pro 16\" M3 Max",
                totalAmount = 2499.00,
                currency = "$",
                purchaseDateMillis = macbookPurchase,
                warrantyMonths = 24,
                warrantyExpiryDateMillis = macbookExpiry,
                category = ReceiptCategory.ELECTRONICS.name,
                notes = "AppleCare+ 2-Year Plan included. Covers accidental damage.",
                imagePath = null,
                rawOcrText = "APPLE STORE #R102\nMACBOOK PRO 16-INCH M3 MAX\nAPPLECARE+ 2 YR WARRANTY\nSUBTOTAL: $2,499.00\nTAX: $206.17\nTOTAL: $2,705.17\nTHANK YOU FOR SHOPPING AT APPLE",
                confidenceScore = 0.96f,
                reminderEnabled = true,
                reminderDaysBefore = 7,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Best Buy",
                itemName = "Sony 65\" Bravia XR OLED 4K TV",
                totalAmount = 1799.99,
                currency = "$",
                purchaseDateMillis = tvPurchase,
                warrantyMonths = 24,
                warrantyExpiryDateMillis = tvExpiry,
                category = ReceiptCategory.APPLIANCES.name,
                notes = "Geek Squad 2-Year Protection Plan with in-home repair.",
                imagePath = null,
                rawOcrText = "BEST BUY STORE #0412\nSONY 65 INCH BRAVIA XR OLED TV\nMODEL: XR-65A80L\n2 YEAR EXTENDED WARRANTY INCLUDED\nTOTAL: $1,799.99\nAUTH CODE: 489211",
                confidenceScore = 0.94f,
                reminderEnabled = true,
                reminderDaysBefore = 14,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Dyson",
                itemName = "Dyson V15 Detect Cordless Vacuum",
                totalAmount = 649.99,
                currency = "$",
                purchaseDateMillis = vacuumPurchase,
                warrantyMonths = 12,
                warrantyExpiryDateMillis = vacuumExpiry,
                category = ReceiptCategory.APPLIANCES.name,
                notes = "1-Year Manufacturer Warranty on motor & battery.",
                imagePath = null,
                rawOcrText = "DYSON DIRECT STORE\nDYSON V15 DETECT CORDLESS VACUUM\n1 YEAR LIMITED WARRANTY\nAMOUNT: $649.99\nPAYMENT: VISA ENDING 4019",
                confidenceScore = 0.91f,
                reminderEnabled = true,
                reminderDaysBefore = 7,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "The Home Depot",
                itemName = "DeWalt 20V MAX Brushless Hammer Drill Kit",
                totalAmount = 299.00,
                currency = "$",
                purchaseDateMillis = drillPurchase,
                warrantyMonths = 36,
                warrantyExpiryDateMillis = drillExpiry,
                category = ReceiptCategory.TOOLS.name,
                notes = "3-Year Limited Warranty, 1-Year Free Service.",
                imagePath = null,
                rawOcrText = "THE HOME DEPOT #6819\nDEWALT 20V MAX CORDLESS DRILL COMBO\n3 YEAR LIMITED WARRANTY INCLUDED\nTOTAL: $299.00\nREGISTER AT DEWALT.COM",
                confidenceScore = 0.95f,
                reminderEnabled = true,
                reminderDaysBefore = 30,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Patagonia",
                itemName = "Nano Puff Insulated Jacket",
                totalAmount = 239.00,
                currency = "$",
                purchaseDateMillis = jacketPurchase,
                warrantyMonths = 3,
                warrantyExpiryDateMillis = jacketExpiry,
                category = ReceiptCategory.CLOTHING.name,
                notes = "3-Month store return warranty, Ironclad guarantee for repairs.",
                imagePath = null,
                rawOcrText = "PATAGONIA SOHO\nNANO PUFF JACKET BLACK M\nPRICE: $239.00\nTAX: $21.21\nTOTAL: $260.21\nIRONCLAD GUARANTEE",
                confidenceScore = 0.88f,
                reminderEnabled = true,
                reminderDaysBefore = 7,
                createdAt = now,
                updatedAt = now
            )
        )
        dao.insertAll(samples)
    }
}

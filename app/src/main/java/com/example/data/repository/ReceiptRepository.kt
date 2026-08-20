package com.example.data.repository

import android.content.Context
import com.example.data.db.ReceiptDao
import com.example.data.db.ReceiptDatabase
import com.example.data.db.ReceiptEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem
import com.example.domain.model.WarrantyStatus
import com.example.domain.parser.ReceiptParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class VaultStats(
    val totalReceiptsCount: Int = 0,
    val activeWarrantiesCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val totalVaultValue: Double = 0.0,
    val currency: String = "₹"
)

class ReceiptRepository(private val context: Context) {
    private val dao: ReceiptDao = ReceiptDatabase.getDatabase(context).receiptDao()
    private val userPrefs: UserPreferencesRepository = UserPreferencesRepository(context)

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

    val vaultStats: Flow<VaultStats> = combine(dao.getAllReceipts(), userPrefs.defaultCurrency) { list, prefCurrency ->
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
            currency = prefCurrency
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

    suspend fun updateAllCurrencies(currency: String) {
        dao.updateAllCurrencies(currency)
    }

    suspend fun seedSampleReceiptsIfEmpty() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val prefCurrency = userPrefs.defaultCurrency.first()

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

        // 4. Expired item (purchased 14 months ago, 3 mo warranty)
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
                merchantName = "Apple BKC",
                itemName = "MacBook Pro 14\" M3 Pro",
                totalAmount = 199900.00,
                currency = "₹",
                purchaseDateMillis = macbookPurchase,
                warrantyMonths = 24,
                warrantyExpiryDateMillis = macbookExpiry,
                category = ReceiptCategory.ELECTRONICS.name,
                notes = "AppleCare+ 2-Year Protection Plan included. Covers accidental damage.",
                imagePath = null,
                rawOcrText = "APPLE BKC MUMBAI\nMACBOOK PRO 14-INCH M3 PRO\nAPPLECARE+ 2 YR WARRANTY\nTOTAL: ₹1,99,900.00\nTHANK YOU FOR SHOPPING AT APPLE BKC",
                confidenceScore = 0.96f,
                reminderEnabled = true,
                reminderDaysBefore = 7,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Croma",
                itemName = "Sony 55\" Bravia 4K Google TV",
                totalAmount = 64990.00,
                currency = "₹",
                purchaseDateMillis = tvPurchase,
                warrantyMonths = 24,
                warrantyExpiryDateMillis = tvExpiry,
                category = ReceiptCategory.APPLIANCES.name,
                notes = "2-Year Comprehensive Manufacturer Warranty + Extended Protection Shield.",
                imagePath = null,
                rawOcrText = "CROMA ELECTRONICS #0412\nSONY 55 INCH BRAVIA 4K SMART TV\nMODEL: KD-55X74L\n2 YEAR EXTENDED WARRANTY INCLUDED\nTOTAL: ₹64,990.00\nINVOICE: CR-984122",
                confidenceScore = 0.94f,
                reminderEnabled = true,
                reminderDaysBefore = 14,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Dyson India",
                itemName = "Dyson V12 Detect Slim Vacuum",
                totalAmount = 45900.00,
                currency = "₹",
                purchaseDateMillis = vacuumPurchase,
                warrantyMonths = 12,
                warrantyExpiryDateMillis = vacuumExpiry,
                category = ReceiptCategory.APPLIANCES.name,
                notes = "1-Year Official Manufacturer Warranty on motor, battery, and attachments.",
                imagePath = null,
                rawOcrText = "DYSON INDIA STORE\nDYSON V12 DETECT SLIM VACUUM\n1 YEAR LIMITED WARRANTY\nAMOUNT: ₹45,900.00\nPAYMENT: UPI HDFC",
                confidenceScore = 0.91f,
                reminderEnabled = true,
                reminderDaysBefore = 7,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Vijay Sales",
                itemName = "Bosch 8kg Front Load Washer",
                totalAmount = 36990.00,
                currency = "₹",
                purchaseDateMillis = drillPurchase,
                warrantyMonths = 36,
                warrantyExpiryDateMillis = drillExpiry,
                category = ReceiptCategory.APPLIANCES.name,
                notes = "3-Year Comprehensive Warranty, 10-Year Motor Warranty.",
                imagePath = null,
                rawOcrText = "VIJAY SALES RETAIL #6819\nBOSCH 8KG FRONT LOAD WASHER\n3 YEAR COMPREHENSIVE WARRANTY\nTOTAL: ₹36,990.00\nREGISTER AT BOSCH-HOME.IN",
                confidenceScore = 0.95f,
                reminderEnabled = true,
                reminderDaysBefore = 30,
                createdAt = now,
                updatedAt = now
            ),
            ReceiptEntity(
                merchantName = "Decathlon India",
                itemName = "Triban RC100 Road Bike",
                totalAmount = 24999.00,
                currency = "₹",
                purchaseDateMillis = jacketPurchase,
                warrantyMonths = 3,
                warrantyExpiryDateMillis = jacketExpiry,
                category = ReceiptCategory.VEHICLES.name,
                notes = "3-Month Free Service Checkup, Lifetime warranty on frame.",
                imagePath = null,
                rawOcrText = "DECATHLON SPORTS INDIA\nTRIBAN RC100 ROAD BIKE\nPRICE: ₹24,999.00\nTOTAL: ₹24,999.00\nKEEP INVOICE FOR WARRANTY",
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

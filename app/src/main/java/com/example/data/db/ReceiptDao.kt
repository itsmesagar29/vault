package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY purchaseDateMillis DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    fun getReceiptById(id: Long): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptByIdDirect(id: Long): ReceiptEntity?

    @Query("""
        SELECT * FROM receipts 
        WHERE warrantyExpiryDateMillis >= :currentTime 
          AND warrantyExpiryDateMillis <= :thresholdTime 
        ORDER BY warrantyExpiryDateMillis ASC
    """)
    fun getExpiringSoonReceipts(currentTime: Long, thresholdTime: Long): Flow<List<ReceiptEntity>>

    @Query("""
        SELECT * FROM receipts 
        WHERE warrantyExpiryDateMillis >= :currentTime 
          AND warrantyExpiryDateMillis <= :thresholdTime
          AND reminderEnabled = 1
    """)
    suspend fun getExpiringReceiptsForReminder(currentTime: Long, thresholdTime: Long): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE warrantyExpiryDateMillis >= :currentTime ORDER BY warrantyExpiryDateMillis ASC")
    fun getActiveReceipts(currentTime: Long): Flow<List<ReceiptEntity>>

    @Query("""
        SELECT * FROM receipts 
        WHERE merchantName LIKE '%' || :query || '%' 
           OR itemName LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%' 
           OR rawOcrText LIKE '%' || :query || '%'
        ORDER BY purchaseDateMillis DESC
    """)
    fun searchReceipts(query: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE category = :category ORDER BY purchaseDateMillis DESC")
    fun getReceiptsByCategory(category: String): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(entity: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ReceiptEntity>): List<Long>

    @Update
    suspend fun updateReceipt(entity: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(entity: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: Long)

    @Query("DELETE FROM receipts")
    suspend fun clearAllReceipts()

    @Query("SELECT COUNT(*) FROM receipts")
    fun getReceiptCount(): Flow<Int>
}

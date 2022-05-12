package com.storyous.bills.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
@Suppress("UnnecessaryAbstractClass")
abstract class BillsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCachedBill(bill: CachedBill)

    @Transaction
    open suspend fun insertOrUpdateCachedBills(
        bills: List<CachedBill>,
        onUpdateMapping: (CachedBill, CachedBill) -> CachedBill
    ) {
        bills.forEach { newBill ->
            if (newBill.items.isNotEmpty()) {
                newBill
            } else {
                getCachedBill(newBill.billId)?.let { bill -> onUpdateMapping(newBill, bill) }
            }?.also {
                insertCachedBill(it)
            }
        }
    }

    @Query("DELETE FROM CachedBill WHERE billId = :billId")
    abstract suspend fun deleteCachedBill(billId: String)

    @Transaction
    @Query("SELECT * FROM CachedBill WHERE billId = :billId")
    abstract suspend fun getCachedBill(billId: String): CachedBill?

    @Transaction
    @Query("SELECT * FROM CachedBill ORDER BY createdAt DESC LIMIT :start, :size")
    abstract suspend fun getCachedBills(start: Int, size: Int): List<CachedBill>

    @Transaction
    @Query("UPDATE CachedBill SET fiscalData=:fiscalData WHERE billId=:billId")
    abstract suspend fun updateBillFiscalData(billId: String, fiscalData: String?)

    @Transaction
    @Query("UPDATE CachedBill SET refunded=1 WHERE billId=:billId")
    abstract suspend fun setRefunded(billId: String)
}

package com.storyous.bills.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.util.Date

@Dao
@Suppress("UnnecessaryAbstractClass")
abstract class BillsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCachedBill(bill: CachedBill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCachedBills(bills: List<CachedBill>)

    @Query("DELETE FROM CachedBill WHERE billId = :billId")
    abstract suspend fun deleteCachedBill(billId: String)

    @Transaction
    @Query("SELECT * FROM CachedBill WHERE billId = :billId")
    abstract suspend fun getCachedBill(billId: String): CachedBill?

    @Transaction
    @Query("SELECT * FROM CachedBill ORDER BY createdAt DESC LIMIT :start, :size")
    abstract suspend fun getCachedBills(start: Int, size: Int): List<CachedBill>

    @Transaction
    @Query("UPDATE CachedBill SET refunded=1 WHERE billId=:billId")
    abstract suspend fun setRefunded(billId: String)
}

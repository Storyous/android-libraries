package com.storyous.delivery.common.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.storyous.commonutils.TimestampUtil
import java.util.Calendar
import java.util.Date

@Suppress("TooManyFunctions")
@Dao
abstract class DeliveryDao {

    @Query("DELETE FROM DeliveryOrder")
    abstract suspend fun deleteOrders()

    @Delete
    abstract suspend fun deleteOrders(orders: List<DeliveryOrder>)

    @Query("DELETE FROM DeliveryOrder WHERE lastModifiedAt < :date AND deliveryTime < :date")
    abstract suspend fun deleteOrdersOlderThan(date: Date)

    @Transaction
    @Query("SELECT * FROM DeliveryOrder ORDER BY deliveryTime DESC")
    abstract suspend fun getOrders(): List<DeliveryOrder>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder ORDER BY deliveryTime DESC")
    abstract fun getOrdersLive(): LiveData<List<DeliveryOrder>>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE state = :state")
    abstract suspend fun getOrders(state: String): List<DeliveryOrder>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE state = :state")
    abstract fun getOrdersLive(state: String): LiveData<List<DeliveryOrder>>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE orderId = :orderId")
    abstract suspend fun getOrder(orderId: String): DeliveryOrder?

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE orderId = :orderId")
    abstract fun getOrderLive(orderId: String): LiveData<DeliveryOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrders(orders: List<DeliveryOrder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrder(order: DeliveryOrder)

    @Transaction
    open suspend fun delete() {
        deleteOrders()
    }

    @Transaction
    open suspend fun update(orders: List<DeliveryOrder>) {
        insertOrders(orders)
        deleteOrdersOlderThan(TimestampUtil.getCalendar().apply { add(Calendar.DATE, -1) }.time)
    }
}

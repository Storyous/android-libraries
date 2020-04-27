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

    @Query("DELETE FROM Customer")
    abstract suspend fun deleteCustomers()

    @Delete
    abstract suspend fun deleteOrders(orders: List<DeliveryOrder>)

    @Delete
    abstract suspend fun deleteCustomers(customers: List<Customer>)

    @Transaction
    @Query("SELECT * FROM DeliveryOrder ORDER BY deliveryTime DESC")
    abstract suspend fun getOrders(): List<DeliveryOrderWithCustomer>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder ORDER BY deliveryTime DESC")
    abstract fun getOrdersLive(): LiveData<List<DeliveryOrderWithCustomer>>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE state = :state ORDER BY deliveryTime DESC")
    abstract fun getOrdersLive(state: String): LiveData<List<DeliveryOrderWithCustomer>>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE lastModifiedAt < :date OR deliveryTime < :date")
    abstract suspend fun getOldOrders(date: Date): List<DeliveryOrderWithCustomer>

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE orderId = :orderId")
    abstract suspend fun getOrder(orderId: String): DeliveryOrderWithCustomer?

    @Transaction
    @Query("SELECT * FROM DeliveryOrder WHERE orderId = :orderId")
    abstract fun getOrderLive(orderId: String): LiveData<DeliveryOrderWithCustomer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrders(orders: List<DeliveryOrder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCustomers(customers: List<Customer>)

    @Transaction
    open suspend fun delete() {
        deleteCustomers()
        deleteOrders()
    }

    @Suppress("ReturnCount")
    @Transaction
    open suspend fun deleteOrdersOlderThan(date: Date) {
        val oldOrders = getOldOrders(date)
            .takeIf { it.isNotEmpty() } ?: return

        deleteOrders(oldOrders.map { it.order })
        deleteCustomers(oldOrders.map { it.customer })
    }

    @Transaction
    open suspend fun update(orders: List<DeliveryOrderWithCustomer>) {
        insertOrders(orders.map { it.order })
        insertCustomers(orders.map { it.customer })
        deleteOrdersOlderThan(TimestampUtil.getCalendar().apply { add(Calendar.DATE, -1) }.time)
    }
}

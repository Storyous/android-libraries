package com.storyous.delivery.common.db

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

    @Query("SELECT * FROM DeliveryOrder ORDER BY deliveryTime DESC")
    abstract suspend fun getOrders(): List<DeliveryOrder>

    @Query("SELECT * FROM DeliveryOrder WHERE lastModifiedAt < :date OR deliveryTime < :date")
    abstract suspend fun getOldOrders(date: Date): List<DeliveryOrder>

    @Query("SELECT * FROM DeliveryOrder WHERE orderId = :orderId")
    abstract suspend fun getOrder(orderId: String): DeliveryOrder?

    @Query("SELECT * FROM Customer WHERE id = :customerId")
    abstract suspend fun getCustomer(customerId: String): Customer?

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
        deleteOrders(oldOrders)
        deleteCustomers(oldOrders.mapNotNull { it.customer })
    }

    @Transaction
    open suspend fun getCompleteOrders(): List<DeliveryOrder> {
        return getOrders().apply {
            forEach { order: DeliveryOrder ->
                order.customer = order.customerId?.let { getCustomer(it) }
            }
        }
    }

    @Transaction
    open suspend fun getCompleteOrder(orderId: String): DeliveryOrder? {
        return getOrder(orderId)?.apply {
            customer = customerId?.let { getCustomer(it) }
        }
    }

    @Transaction
    open suspend fun storeCompleteOrders(orders: List<DeliveryOrder>) {
        insertOrders(orders)
        insertCustomers(orders.mapNotNull { it.customer })
    }

    @Transaction
    open suspend fun updateAndGetAll(orders: List<DeliveryOrder>): List<DeliveryOrder> {
        storeCompleteOrders(orders)
        deleteOrdersOlderThan(TimestampUtil.getCalendar().apply { add(Calendar.DATE, -1) }.time)
        return getCompleteOrders()
    }
}

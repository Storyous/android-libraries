package com.storyous.delivery.common.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.util.Date

@Suppress("TooManyFunctions")
@Dao
abstract class DeliveryDao {

    @Query("DELETE FROM DeliveryOrder")
    abstract suspend fun deleteOrders()

    @Query("DELETE FROM Customer")
    abstract suspend fun deleteCustomers()

    @Query("DELETE FROM DeliveryItem")
    abstract suspend fun deleteItems()

    @Query("DELETE FROM DeliveryAddition")
    abstract suspend fun deleteAdditions()

    @Delete
    abstract suspend fun deleteOrders(orders: List<DeliveryOrder>)

    @Delete
    abstract suspend fun deleteCustomers(customers: List<Customer>)

    @Delete
    abstract suspend fun deleteItems(items: List<DeliveryItem>)

    @Delete
    abstract suspend fun deleteAdditions(additions: List<DeliveryAddition>)

    @Query("SELECT * FROM DeliveryOrder ORDER BY deliveryTime DESC")
    abstract suspend fun getOrders(): List<DeliveryOrder>

    @Query("SELECT * FROM DeliveryOrder WHERE lastModifiedAt < :date OR deliveryTime < :date")
    abstract suspend fun getOldOrders(date: Date): List<DeliveryOrder>

    @Query("SELECT * FROM DeliveryOrder WHERE orderId = :orderId")
    abstract suspend fun getOrder(orderId: String): DeliveryOrder?

    @Query("SELECT * FROM Customer WHERE id = :customerId")
    abstract suspend fun getCustomer(customerId: String): Customer?

    @Query("SELECT * FROM DeliveryItem WHERE orderId = :orderId")
    abstract suspend fun getItems(orderId: String): List<DeliveryItem>

    @Query("SELECT * FROM DeliveryAddition WHERE parentItemId = :parentItemId")
    abstract suspend fun getAdditions(parentItemId: String): List<DeliveryAddition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrders(orders: List<DeliveryOrder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCustomers(customers: List<Customer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItems(items: List<DeliveryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAdditions(additions: List<DeliveryAddition>)

    @Transaction
    open suspend fun delete() {
        deleteCustomers()
        deleteAdditions()
        deleteItems()
        deleteOrders()
    }

    @Transaction
    open suspend fun deleteOrdersOlderThan(date: Date) {
        val oldOrders = getOldOrders(date)
            .takeIf { it.isNotEmpty() } ?: return
        deleteOrders(oldOrders)
        deleteCustomers(oldOrders.mapNotNull { it.customer })

        val items = oldOrders.map { it.items }.flatten()
            .takeIf { it.isNotEmpty() } ?: return
        deleteItems(items)

        val additions = items.mapNotNull { it.additions }.flatten()
            .takeIf { it.isNotEmpty() } ?: return
        deleteAdditions(additions)
    }

    @Transaction
    open suspend fun getCompleteOrders(): List<DeliveryOrder> {
        return getOrders().apply {
            forEach { order: DeliveryOrder ->
                order.customer = order.customerId?.let { getCustomer(it) }
                order.items = getItems(order.orderId)
                order.items.forEach {
                    it.additions = getAdditions(it.itemId)
                }
            }
        }
    }

    @Transaction
    open suspend fun getCompleteOrder(orderId: String): DeliveryOrder? {
        return getOrder(orderId)?.apply {
            customer = customerId?.let { getCustomer(it) }
            items = getItems(orderId)
            items.forEach {
                it.additions = getAdditions(it.itemId)
            }
        }
    }

    @Transaction
    open suspend fun storeCompleteOrders(orders: List<DeliveryOrder>) {
        insertOrders(orders)
        insertCustomers(orders.mapNotNull { it.customer })
        val items = orders.map { it.items }.flatten()
        insertItems(items)
        val additions = items.mapNotNull { it.additions }.flatten()
        insertAdditions(additions)
    }
}

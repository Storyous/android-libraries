package com.storyous.delivery.common.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.DeliveryErrorConverterWrapper
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.DeliveryService
import com.storyous.delivery.common.api.RequestDeclineBody
import com.storyous.delivery.common.db.DeliveryDao
import com.storyous.delivery.common.toApi
import com.storyous.delivery.common.toDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

@Suppress("TooManyFunctions")
open class DeliveryRepository(
    private val apiService: () -> DeliveryService,
    private val db: DeliveryDao
) : CoroutineScope by CoroutineProviderScope() {

    companion object {
        const val STATUS_CODE_UNAUTHORIZED = 401
        const val STATUS_CODE_CONFLICT = 409
    }

    val newOrdersLive = db.getOrdersLive(DeliveryOrder.STATE_NEW).toApi()
    val dispatchedOrdersLive = db.getOrdersLive(DeliveryOrder.STATE_DISPATCHED).toApi()
    val deliveryOrdersLive = db.getOrdersLive().toApi()
    private val deliveryError = MutableLiveData<DeliveryException?>()
    private var lastMod: String? = null

    val ringingState = MediatorLiveData<Boolean>().apply {
        addSource(newOrdersLive) { value = it.isNotEmpty() }
    }

    fun getDeliveryError(): LiveData<DeliveryException?> = deliveryError

    suspend fun loadDeliveryOrders(
        merchantId: String,
        placeId: String
    ) = withContext(provider.IO) {
        runCatching {
            apiService().getDeliveryOrdersAsync(merchantId, placeId, lastMod)
                .also { response ->
                    db.update(response.data.map { it.toDb() })
                    lastMod = response.lastModificationAt
                }
        }
    }.onFailure {
        Timber.e(it, "Fail to load Delivery orders.")
        if (it is HttpException && it.code() == STATUS_CODE_UNAUTHORIZED) {
            deliveryError.value = DeliveryException(cause = it)
        }
    }.getOrNull()


    @Throws(DeliveryException::class)
    suspend fun confirmDeliveryOrder(
        merchantId: String,
        placeId: String,
        order: DeliveryOrder
    ) = handleOrderUpdate {
        apiService().confirmDeliveryOrderAsync(merchantId, placeId, order.orderId)
    }

    @Throws(DeliveryException::class)
    suspend fun declineDeliveryOrder(
        merchantId: String,
        placeId: String,
        order: DeliveryOrder,
        reason: String
    ) = handleOrderUpdate {
        apiService().declineDeliveryOrderAsync(
            merchantId,
            placeId,
            order.orderId,
            RequestDeclineBody(reason)
        )
    }

    @Throws(DeliveryException::class)
    suspend fun dispatchDeliveryOrder(
        merchantId: String,
        placeId: String,
        order: DeliveryOrder
    ) = handleOrderUpdate {
        apiService().notifyOrderDispatched(merchantId, placeId, order.orderId)
    }

    @Throws(DeliveryException::class)
    private suspend fun handleOrderUpdate(
        block: suspend () -> DeliveryOrder
    ) = runCatching { block() }
        .onSuccess { db.insertOrder(it.toDb()) }
        .recoverCatching {
            val error = DeliveryErrorConverterWrapper.INSTANCE?.convertPosError(it)

            error?.order?.run { db.insertOrder(toDb()) }

            if (error?.code == STATUS_CODE_CONFLICT) {
                throw DeliveryException(ERR_CONFLICT)
            } else {
                throw DeliveryException(ERR_BASE)
            }
        }
        .getOrThrow()

    suspend fun getOrder(orderId: String) = db.getOrder(orderId)?.toApi()

    fun getOrderLive(orderId: String) = db.getOrderLive(orderId).map {
        it.toApi()
    }

    suspend fun getNewOrdersFromDb() = db.getOrders(DeliveryOrder.STATE_NEW).map { it.toApi() }

    suspend fun getConfirmedOrdersFromDb() =
        db.getOrders(DeliveryOrder.STATE_CONFIRMED).map { it.toApi() }

    suspend fun clear() {
        withContext(provider.IO) {
            db.delete()
        }
        lastMod = null
        deliveryError.value?.let { deliveryError.value = null }
    }

    fun getOrdersLive(states: List<String>) = db.getOrdersLive(states).toApi()
}

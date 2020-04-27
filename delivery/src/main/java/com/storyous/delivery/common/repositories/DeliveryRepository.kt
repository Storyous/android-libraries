package com.storyous.delivery.common.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.DeliveryErrorConverterWrapper
import com.storyous.delivery.common.api.DeliveryService
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.api.model.OrderProviderInfo
import com.storyous.delivery.common.api.model.RequestDeclineBody
import com.storyous.delivery.common.db.DeliveryDao
import com.storyous.delivery.common.toApi
import com.storyous.delivery.common.toDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("TooManyFunctions")
open class DeliveryRepository(
    private val apiService: () -> DeliveryService,
    private val db: DeliveryDao
) : CoroutineScope by CoroutineProviderScope() {

    companion object {
        const val RESULT_OK = "ok"
        const val RESULT_ERR_CONFLICT = "conflict_state"
        const val STATUS_CODE_UNAUTHORIZED = 401
        const val STATUS_CODE_CONFLICT = 409
    }

    private val confirmedOrdersQueue = ConcurrentLinkedQueue<DeliveryOrder>()
    private val confirmedOrders = MutableLiveData<DeliveryOrder>()
    val dispatchedOrdersLive = db.getOrdersLive(DeliveryOrder.STATE_DISPATCHED).toApi()
    val deliveryOrdersLive = db.getOrdersLive().toApi()
    private val deliveryError = MutableLiveData<DeliveryException>()
    private var lastMod: String? = null

    val newDeliveriesToHandle = Transformations.map(deliveryOrdersLive) { deliveries ->
        deliveries.filter { it.state == DeliveryOrder.STATE_NEW }
    }
    val ringingState = MediatorLiveData<Boolean>().apply {
        addSource(newDeliveriesToHandle) { value = it.isNotEmpty() }
    }

    fun getConfirmedOrders(): LiveData<DeliveryOrder> = confirmedOrders
    fun getDeliveryError(): LiveData<DeliveryException> = deliveryError

    fun addConfirmedOrder(order: DeliveryOrder) {
        confirmedOrdersQueue.add(order)
        confirmedOrders.value = confirmedOrdersQueue.peek()
    }

    fun removeConfirmedOrder(order: DeliveryOrder) {
        confirmedOrdersQueue.remove(order)
        confirmedOrders.value = confirmedOrdersQueue.peek()
    }

    private suspend fun updateOrdersInDb(orders: List<DeliveryOrder>) = withContext(provider.IO) {
        db.update(orders.map { it.toDb() })
    }

    suspend fun loadDeliveryOrders(
        merchantId: String,
        placeId: String
    ) = withContext(provider.Main) {
        runCatching {
            withContext(provider.IO) {
                apiService().getDeliveryOrdersAsync(merchantId, placeId, lastMod)
            }
        }.onFailure {
            Timber.e(it, "Fail to load Delivery orders.")
            if (it is HttpException && it.code() == STATUS_CODE_UNAUTHORIZED) {
                deliveryError.value = DeliveryException(it)
            }
        }.onSuccess {
            updateOrdersInDb(it.data)
            lastMod = it.lastModificationAt
        }.getOrNull()
    }

    suspend fun acceptDeliveryOrder(
        merchantId: String,
        placeId: String,
        order: DeliveryOrder
    ): String {
        var retval = ""

        runCatching {
            apiService().confirmDeliveryOrderAsync(merchantId, placeId, order.orderId)
        }.onSuccess {
            retval = RESULT_OK
        }.getOrElse {
            val error = DeliveryErrorConverterWrapper.INSTANCE?.convertPosError(it)

            if (error?.code == STATUS_CODE_CONFLICT) {
                retval = RESULT_ERR_CONFLICT
            }
            Timber.e(it, "Fail to confirm order${order.orderId}.")

            error?.order
        }?.also {
            updateOrder(it)
        }

        return retval
    }

    suspend fun cancelDeliveryOrder(
        merchantId: String,
        placeId: String,
        order: DeliveryOrder,
        reason: String
    ): String {
        var retval = ""

        runCatching {
            apiService().declineDeliveryOrderAsync(
                merchantId,
                placeId,
                order.orderId,
                RequestDeclineBody(reason)
            )
        }.onSuccess {
            retval = RESULT_OK
        }.getOrElse {
            val error = DeliveryErrorConverterWrapper.INSTANCE?.convertPosError(it)

            if (error?.code == STATUS_CODE_CONFLICT) {
                retval = RESULT_ERR_CONFLICT
            }
            Timber.e(it, "Fail to decline order${order.orderId}.")

            error?.order
        }?.also {
            updateOrder(it)
        }

        return retval
    }

    suspend fun notifyDeliveryOrderDispatched(
        merchantId: String,
        placeId: String,
        order: DeliveryOrder
    ): String {
        return order.takeIf { it.provider != "covermanager" }?.let {
            notifyDeliveryOrderDispatched(merchantId, placeId, it.orderId)
        } ?: ""
    }

    fun notifyDeliveryOrderDispatched(
        merchantId: String,
        placeId: String,
        order: OrderProviderInfo?
    ) {
        order?.takeIf { it.code != "covermanager" }?.let { providerInfo ->
            launch {
                notifyDeliveryOrderDispatched(merchantId, placeId, providerInfo.orderId)
            }
        }
    }

    private suspend fun notifyDeliveryOrderDispatched(
        merchantId: String,
        placeId: String,
        orderId: String
    ): String {
        var retval = ""

        runCatching {
            apiService().notifyOrderDispatched(merchantId, placeId, orderId)
        }.onSuccess {
            retval = RESULT_OK
        }.getOrElse {
            val error = DeliveryErrorConverterWrapper.INSTANCE?.convertPosError(it)

            if (error?.code == STATUS_CODE_CONFLICT) {
                retval = RESULT_ERR_CONFLICT
            }
            Timber.e(it, "Fail to dispatch order $orderId.")

            error?.order
        }?.also {
            updateOrder(it)
        }

        return retval
    }

    private suspend fun updateOrder(order: DeliveryOrder) {
        updateOrdersInDb(listOf(order))
    }

    fun getOrderLive(orderId: String?): LiveData<DeliveryOrder>? {
        return orderId?.let { Transformations.map(db.getOrderLive(it)) { order -> order?.toApi() } }
    }
}

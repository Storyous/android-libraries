package com.storyous.delivery.common.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.TimestampUtil
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
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class DeliveryRepository(
    private val apiService: () -> DeliveryService,
    private val db: DeliveryDao
) : CoroutineScope by CoroutineProviderScope() {

    companion object {
        const val RESULT_OK = "ok"
        const val RESULT_ERR_CONFLICT = "conflict_state"
        const val STATUS_CODE_CONFLICT = 409
    }

    private val confirmedOrdersQueue = ConcurrentLinkedQueue<DeliveryOrder>()
    private val confirmedOrders = MutableLiveData<DeliveryOrder>()
    private val deliveryOrders = MutableLiveData<List<DeliveryOrder>>()
    private val deliveryError = MutableLiveData<DeliveryException>()
    private var lastMod: String? = null

    val newDeliveriesToHandle = Transformations.map(deliveryOrders) { deliveries ->
        deliveries.filter { it.state == DeliveryOrder.STATE_NEW }
    }
    val ringingState = MediatorLiveData<Boolean>().apply {
        addSource(newDeliveriesToHandle) { value = it.isNotEmpty() }
    }

    fun getDeliveryOrders(): LiveData<List<DeliveryOrder>> = deliveryOrders
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
        db.storeCompleteOrders(orders.map { it.toDb() })
        db.deleteOrdersOlderThan(TimestampUtil.getCalendar().apply { add(Calendar.DATE, -1) }.time)
        db.getCompleteOrders().map { it.toApi() }
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
            if (it is HttpException && it.code() == 401) {
                deliveryError.value = DeliveryException(it)
            }
        }.onSuccess {
            deliveryOrders.value = updateOrdersInDb(it.data)
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

    fun notifyDeliveryOrderDispatched(
        merchantId: String,
        placeId: String,
        order: OrderProviderInfo?
    ) {
        // TODO add param responsible to recognize notification requirement
        order?.takeIf { it.code != "covermanager" }?.let { providerInfo ->
            launch {
                runCatching {
                    withContext(provider.IO) {
                        apiService().notifyOrderDispatched(
                            merchantId, placeId, providerInfo.orderId
                        )
                    }
                }.onFailure {
                    Timber.e(it, "Fail to notify order ${providerInfo.orderId}.")
                }.getOrNull()?.also {
                    updateOrder(it)
                }
            }
        }
    }

    private suspend fun updateOrder(order: DeliveryOrder) = withContext(provider.Main) {
        deliveryOrders.value = updateOrdersInDb(listOf(order))
    }

    suspend fun findOrder(orderId: String): DeliveryOrder? = withContext(provider.IO) {
        deliveryOrders.value?.find { it.orderId == orderId }
            ?: db.getCompleteOrder(orderId)?.toApi()
    }

    class DeliveryException(cause: Exception) : java.lang.Exception(cause) {
        private var consumed = false

        fun consume(): Boolean {
            if (!consumed) {
                consumed = true
                return false
            } else {
                return true
            }
        }
    }
}

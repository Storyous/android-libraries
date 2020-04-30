package com.storyous.delivery.common.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.common.PlaceInfo
import com.storyous.delivery.common.api.DeliveryErrorConverterWrapper
import com.storyous.delivery.common.api.DeliveryService
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.api.model.OrderProviderInfo
import com.storyous.delivery.common.api.model.RequestDeclineBody
import com.storyous.delivery.common.db.DeliveryDao
import com.storyous.delivery.common.db.DeliveryOrderWithCustomer
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
        const val RESULT_NONE = ""
        const val RESULT_OK = "ok"
        const val RESULT_ERR_CONFLICT = "conflict_state"
        const val STATUS_CODE_UNAUTHORIZED = 401
        const val STATUS_CODE_CONFLICT = 409
    }

    private val confirmedOrdersQueue = ConcurrentLinkedQueue<DeliveryOrder>()
    private val confirmedOrders = MutableLiveData<DeliveryOrder?>()
    val placeInfo = MutableLiveData<PlaceInfo?>()
    val newOrdersLive = Transformations.switchMap(placeInfo) {
        db.getOrdersLive(
            it?.merchantId ?: "#",
            it?.placeId ?: "#",
            DeliveryOrder.STATE_NEW
        ).toApi()
    }
    val dispatchedOrdersLive = Transformations.switchMap(placeInfo) {
        db.getOrdersLive(
            it?.merchantId ?: "#",
            it?.placeId ?: "#",
            DeliveryOrder.STATE_DISPATCHED
        ).toApi()
    }
    val deliveryOrdersLive = Transformations.switchMap(placeInfo) {
        db.getOrdersLive(
            placeInfo.value?.merchantId ?: "#",
            placeInfo.value?.placeId ?: "#"
        ).toApi()
    }
    private val deliveryError = MutableLiveData<DeliveryException?>()
    private var lastMod: String? = null

    val ringingState = MediatorLiveData<Boolean>().apply {
        addSource(newOrdersLive) { value = it.isNotEmpty() }
    }

    fun getConfirmedOrders(): LiveData<DeliveryOrder?> = confirmedOrders
    fun getDeliveryError(): LiveData<DeliveryException?> = deliveryError

    fun addConfirmedOrder(order: DeliveryOrder) {
        confirmedOrdersQueue.add(order)
        confirmedOrders.value = confirmedOrdersQueue.peek()
    }

    fun removeConfirmedOrder(order: DeliveryOrder) {
        confirmedOrdersQueue.remove(order)
        confirmedOrders.value = confirmedOrdersQueue.peek()
    }

    private suspend fun updateOrdersInDb(
        merchantId: String,
        placeId: String,
        orders: List<DeliveryOrder>,
        lastModification: String?
    ) = withContext(provider.IO) {
        db.update(orders.map { it.toDb(merchantId, placeId) })
        lastMod = lastModification
    }

    suspend fun loadDeliveryOrders() = withContext(provider.Main) {
        val (placeId, merchantId, autoConfirm) = placeInfo.value ?: return@withContext
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
            updateOrdersInDb(merchantId, placeId, it.data, it.lastModificationAt)
        }.getOrNull()
    }

    suspend fun acceptDeliveryOrder(
        order: DeliveryOrder
    ): String {
        var retval = RESULT_NONE
        val (placeId, merchantId, autoConfirm) = placeInfo.value ?: return retval
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
        order: DeliveryOrder,
        reason: String
    ): String {
        var retval = RESULT_NONE
        val (placeId, merchantId, autoConfirm) = placeInfo.value ?: return retval
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
        order: DeliveryOrder
    ): String {
        return order.takeIf { it.provider != "covermanager" }?.let {
            notifyDeliveryOrderDispatched(it.orderId)
        } ?: RESULT_NONE
    }

    fun notifyDeliveryOrderDispatched(
        order: OrderProviderInfo?
    ) {
        order?.takeIf { it.code != "covermanager" }?.let { providerInfo ->
            launch {
                notifyDeliveryOrderDispatched(providerInfo.orderId)
            }
        }
    }

    private suspend fun notifyDeliveryOrderDispatched(
        orderId: String
    ): String {
        var retval = RESULT_NONE
        val (placeId, merchantId, autoConfirm) = placeInfo.value ?: return retval
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
        val (placeId, merchantId, autoConfirm) = placeInfo.value ?: return
        updateOrdersInDb(merchantId, placeId, listOf(order), lastMod)
    }

    fun getOrderLive(orderId: String) = Transformations.switchMap(placeInfo) { info ->
        Transformations.map(db.getOrderLive(info?.merchantId ?: "#", info?.placeId ?: "#", orderId)) { it?.toApi() }
    }

    suspend fun getNewOrdersFromDb() = placeInfo.value?.let {
        db.getOrders(it.merchantId, it.placeId, DeliveryOrder.STATE_NEW).map { it.toApi() }
    } ?: emptyList()

    suspend fun clear() {
        withContext(provider.IO) {
            db.delete()
        }
        lastMod = null
        confirmedOrdersQueue.clear()
        confirmedOrders.value?.let { confirmedOrders.value = null }
        deliveryError.value?.let { deliveryError.value = null }
    }
}

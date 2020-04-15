package com.storyous.delivery.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.TimestampUtil
import com.storyous.commonutils.extensions.getDistinct
import com.storyous.commonutils.provider
import com.storyous.delivery.api.DeliveryErrorConverterWrapper
import com.storyous.delivery.api.model.OrderProviderInfo
import com.storyous.delivery.common.api.model.BaseDataResponse
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.api.model.RequestDeclineBody
import com.storyous.storyouspay.api.DeliveryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import java.util.Date


open class DeliveryRepository(
    private val apiService: () -> DeliveryService
) : CoroutineScope by CoroutineProviderScope() {

    companion object {
        const val RESULT_OK = "ok"
        const val RESULT_ERR_CONFLICT = "conflict_state"
        const val STATUS_CODE_CONFLICT = 409
    }

    private val deliveryOrders = MutableLiveData<List<DeliveryOrder>>()

    val newDeliveriesToHandle = Transformations.map(deliveryOrders.getDistinct()) { deliveries ->
        deliveries.any { it.state == DeliveryOrder.STATE_NEW }
    }
    val ringingState = MediatorLiveData<Boolean>().apply {
        addSource(newDeliveriesToHandle) { value = it }
    }

    fun getDeliveryOrders(): LiveData<List<DeliveryOrder>> = deliveryOrders
    private var lastMod: String? = null

    suspend fun loadDeliveryOrders(merchantId: String, placeId: String) = withContext(provider.Main) {
        runCatching {
            withContext(provider.IO) {
                apiService().getDeliveryOrdersAsync(merchantId, placeId, lastMod)
            }
        }.getOrElse {
            Timber.e(it, "Fail to load Delivery orders.")
            null
        }?.also { response ->
            deliveryOrders.value = filterLastModOrders(response).sortedByDescending { it.deliveryTime }
            lastMod = response.lastModificationAt
        }
    }

    private fun filterLastModOrders(
        response: BaseDataResponse<List<DeliveryOrder>>
    ): List<DeliveryOrder> {
        return if (lastMod == null || deliveryOrders.value == null) {
            response.data
        } else {
            val responseIds = response.data.map { it.orderId }
            response.data +
                (deliveryOrders.value as List<DeliveryOrder>)
                    .filterNot { responseIds.contains(it.orderId) }
        }.filter { lessThanDayOld(it.deliveryTime) || lessThanDayOld(it.lastModifiedAt) }
    }

    private fun lessThanDayOld(isoDate: Date?): Boolean {
        return if (isoDate == null) {
            false
        } else runCatching {
            TimestampUtil.getCalendar()
                .apply { add(Calendar.DATE, -1) }.time
                .let { isoDate > it }
        }.getOrElse {
            Timber.e(it, "Could not parse the date: $isoDate")
            false
        }
    }

    suspend fun acceptDeliveryOrder(merchantId: String, placeId: String, order: DeliveryOrder): String {
        var retval = ""

        runCatching {
            apiService().confirmDeliveryOrderAsync(merchantId, placeId, order.orderId)
                .also { retval = RESULT_OK }
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

    suspend fun cancelDeliveryOrder(merchantId: String, placeId: String, order: DeliveryOrder, reason: String): String {
        var retval = ""

        runCatching {
            apiService().declineDeliveryOrderAsync(merchantId, placeId, order.orderId,
                RequestDeclineBody(reason))
                .also { retval = RESULT_OK }
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

    fun notifyDeliveryOrderDispatched(merchantId: String, placeId: String, order: OrderProviderInfo?) {
        // TODO add param responsible to recognize notification requirement
        order?.takeIf { it.code != "covermanager" }?.let { providerInfo ->
            launch {
                runCatching {
                    withContext(provider.IO) {
                        apiService().notifyOrderDispatched(merchantId, placeId, providerInfo
                            .orderId)
                    }
                }.getOrElse {
                    Timber.e(it, "Fail to notify order ${providerInfo.orderId}.")
                    null
                }?.also {
                    updateOrder(it)
                }
            }
        }
    }

    private fun updateOrder(receivedOrder: DeliveryOrder) {
        deliveryOrders.value?.find { it.orderId == receivedOrder.orderId }?.also {
            it.update(receivedOrder)
            deliveryOrders.postValue(deliveryOrders.value)
        }
    }
}

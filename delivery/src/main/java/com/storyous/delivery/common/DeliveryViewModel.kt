package com.storyous.delivery.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.Result
import com.storyous.commonutils.TimestampUtil
import com.storyous.commonutils.castOrNull
import com.storyous.commonutils.provider
import com.storyous.commonutils.toResult
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryException
import com.storyous.delivery.common.repositories.ERR_CONFLICT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

@Suppress("TooManyFunctions")
class DeliveryViewModel : ViewModel(), CoroutineScope by CoroutineProviderScope() {

    companion object {
        const val PROVIDER_DJ = "dj"
        const val PROVIDER_WOLT = "wolt"
        const val PROVIDER_UBER_EATS = "ubereats"
        const val PROVIDER_DELIVERECT = "deliverect"
        const val PROVIDER_DELIVERY_HERO = "deliveryHero"
        const val PROVIDER_PHONE = "phone"
        const val PROVIDER_STORYOUS_TAKEAWAY_WEB = "5e6e9c3f0ab34eb0f646bc05"
        const val PROVIDER_STORYOUS_TAKEAWAY_APP = "5e1eca6a3a6c2907928d392b"
        const val PROVIDER_STORYOUS_ONEMENU_QR = "5ee74cc2764ce12de93fedaa"

        const val MESSAGE_ERROR_STATE_CONFLICT = 1
        const val MESSAGE_ERROR_OTHER = 2
        const val MESSAGE_OK_DECLINED = 3
    }

    val filteredStatesLive = MutableLiveData(listOf(DeliveryOrder.STATE_CONFIRMED))
    var filteredStates: List<String>
        get() = filteredStatesLive.value ?: listOf()
        set(value) {
            filteredStatesLive.value = value
        }
    private val selectedOrderIdLive = MutableLiveData<String?>(null)
    var selectedOrderId: String?
        get() = selectedOrderIdLive.value
        set(value) {
            selectedOrderIdLive.value = value
            Timber.i("Delivery order selected $value")
        }
    val selectedOrderLive = selectedOrderIdLive.switchMap {
        it?.let {
            DeliveryConfiguration.deliveryRepository!!.getOrderLive(it).map { order ->
                order ?: deselectOrder()
                order
            }
        } ?: MutableLiveData<DeliveryOrder>(null)
    }
    val selectedOrder: DeliveryOrder? get() = selectedOrderLive.value
    val newOrdersLive = DeliveryConfiguration.deliveryRepository!!.getOrdersLive(
        listOf(DeliveryOrder.STATE_NEW, DeliveryOrder.STATE_SCHEDULING_DELIVERY)
    )
    val filteredOrdersLive = filteredStatesLive.switchMap {
        DeliveryConfiguration.deliveryRepository!!.getOrdersLive(it)
    }

    private fun functionsToLive(
        order: DeliveryOrder?,
        block: suspend DeliveryConfiguration.(order: DeliveryOrder) ->
        Pair<Boolean, Boolean>
    ): LiveData<Pair<Boolean, Boolean>> {
        return liveData {
            emit(false to false)
            order?.runCatching { DeliveryConfiguration.block(this) }?.onSuccess { emit(it) }
        }
    }

    val acceptFunction = selectedOrderLive.switchMap { order ->
        functionsToLive(order) { acceptVisible(it) to acceptEnabled(it) }
    }
    val cancelFunction = selectedOrderLive.map {
        with(it?.state == DeliveryOrder.STATE_NEW) {
            this to (this && it?.timing?.autoDeclineAfter ?: Date(Long.MAX_VALUE) > TimestampUtil.getDate())
        }
    }
    val dispatchFunction = selectedOrderLive.switchMap { order ->
        functionsToLive(order) { dispatchVisible(it) to dispatchEnabled(it) }
    }
    val printBillFunction = selectedOrderLive.switchMap { order ->
        functionsToLive(order) { printBillVisible(it) to printBillEnabled(it) }
    }
    val loadingOrderAccepting = MutableLiveData(false)
    val loadingOrderCancelling = MutableLiveData(false)
    val loadingOrderDispatching = MutableLiveData(false)
    val printOrderBillState = MutableLiveData<Result<Any>?>()
    val messagesToShow = MutableLiveData(mutableListOf<Int>())

    fun loadOrders() = DeliveryConfiguration.deliveryModel.loadOrders()

    fun deselectOrder() {
        selectedOrderIdLive.value = null
    }

    fun acceptOrder(order: DeliveryOrder) {
        loadingOrderAccepting.value = true
        // hold data of selected order to prevent change data when is selected different order
        launch(provider.Main) {
            runCatching {
                withContext(provider.IO) {
                    DeliveryConfiguration.deliveryModel.confirm(order)
                }
            }.onFailure {
                when (it.castOrNull<DeliveryException>()?.error) {
                    ERR_CONFLICT -> addMessageToShow(MESSAGE_ERROR_STATE_CONFLICT)
                    else -> addMessageToShow(MESSAGE_ERROR_OTHER)
                }
            }

            loadingOrderAccepting.value = false
        }
    }

    fun cancelOrder(order: DeliveryOrder, reason: String) {
        loadingOrderCancelling.postValue(true)
        launch {
            runCatching {
                withContext(provider.IO) {
                    DeliveryConfiguration.deliveryModel.decline(order, reason)
                }
            }.onSuccess {
                addMessageToShow(MESSAGE_OK_DECLINED)
            }.onFailure {
                when (it.castOrNull<DeliveryException>()?.error) {
                    ERR_CONFLICT -> addMessageToShow(MESSAGE_ERROR_STATE_CONFLICT)
                    else -> addMessageToShow(MESSAGE_ERROR_OTHER)
                }
            }

            loadingOrderCancelling.postValue(false)
        }
    }

    private fun addMessageToShow(messageId: Int) {
        messagesToShow.postValue(messagesToShow.value.also { it?.add(messageId) })
    }

    fun notifyMessagesShown() {
        messagesToShow.value = ArrayList()
    }

    fun stopRinging() {
        DeliveryConfiguration.deliveryRepository?.ringingState?.value = false
    }

    fun dispatchOrder(order: DeliveryOrder) {
        loadingOrderDispatching.postValue(true)
        // hold data of selected order to prevent change data when is selected different order
        launch(provider.Main) {
            runCatching {
                withContext(provider.IO) {
                    DeliveryConfiguration.deliveryModel.dispatch(order)
                }
            }.onFailure {
                when (it.castOrNull<DeliveryException>()?.error) {
                    ERR_CONFLICT -> addMessageToShow(MESSAGE_ERROR_STATE_CONFLICT)
                    else -> addMessageToShow(MESSAGE_ERROR_OTHER)
                }
            }

            loadingOrderDispatching.postValue(false)
        }
    }

    fun printBill(order: DeliveryOrder) = launch {
        printOrderBillState.value = Result.loading()
        printOrderBillState.value = withContext(provider.IO) {
            runCatching {
                DeliveryConfiguration.printBillBy(order.orderId)
            }
        }.onFailure {
            Timber.e(it)
        }.toResult()
    }

    fun refreshFunctions() {
        selectedOrderId = selectedOrderId
    }
}

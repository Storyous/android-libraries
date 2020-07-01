package com.storyous.delivery.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.castOrNull
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryException
import com.storyous.delivery.common.repositories.ERR_CONFLICT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Suppress("TooManyFunctions")
class DeliveryViewModel : ViewModel(), CoroutineScope by CoroutineProviderScope() {

    companion object {
        const val PROVIDER_DJ = "dj"
        const val PROVIDER_UBER_EATS = "ubereats"
        const val PROVIDER_DELIVERECT = "deliverect"
        const val PROVIDER_PHONE = "phone"
        const val PROVIDER_STORYOUS_TAKEAWAY_WEB = "5e6e9c3f0ab34eb0f646bc05"
        const val PROVIDER_STORYOUS_TAKEAWAY_APP = "5e1eca6a3a6c2907928d392b"
        const val PROVIDER_STORYOUS_ONEMENU_QR = "5ee74cc2764ce12de93fedaa"

        const val MESSAGE_ERROR_STATE_CONFLICT = 1
        const val MESSAGE_ERROR_OTHER = 2
        const val MESSAGE_OK_DECLINED = 3
    }

    private val selectedOrderIdLive = MutableLiveData<String>(null)
    private val selectedOrderLive = Transformations.switchMap(selectedOrderIdLive) {
        it?.let { DeliveryConfiguration.deliveryRepository?.getOrderLive(it) }
            ?: MutableLiveData<DeliveryOrder>(null)
    }
    private val deliveryOrdersLive: LiveData<List<DeliveryOrder>> =
        MediatorLiveData<List<DeliveryOrder>>().apply {
            addSource(DeliveryConfiguration.deliveryRepository!!.deliveryOrdersLive) { orders ->
                if (getSelectedOrder()?.orderId?.let { orderId -> orders.find { it.orderId == orderId } } == null) {
                    deselectOrder()
                }
                value = orders
            }
        }

    val acceptFunction = Transformations.switchMap(selectedOrderLive) {
        MutableLiveData(false to false).apply {
            if (it != null) {
                launch {
                    value = withContext(provider.IO) {
                        with(DeliveryConfiguration) {
                            acceptVisible(it) to acceptEnabled(it)
                        }
                    }
                }
            }
        }
    }
    val cancelFunction = Transformations.map(selectedOrderLive) {
        with(it?.state == DeliveryOrder.STATE_NEW) { this to this }
    }
    val dispatchFunction = Transformations.switchMap(selectedOrderLive) {
        MutableLiveData(false to false).apply {
            if (it != null) {
                launch {
                    value = withContext(provider.IO) {
                        DeliveryConfiguration.dispatchVisible(it) to DeliveryConfiguration.dispatchEnabled(
                            it
                        )
                    }
                }
            }
        }
    }
    val loadingOrderAccepting = MutableLiveData(false)
    val loadingOrderCancelling = MutableLiveData(false)
    val loadingOrderDispatching = MutableLiveData(false)
    val messagesToShow = MutableLiveData(mutableListOf<Int>())

    fun loadOrders() {
        DeliveryConfiguration.deliveryModel.loadOrders()
    }

    fun deselectOrder() {
        selectedOrderIdLive.value = null
    }

    fun setSelectOrder(orderId: String?) {
        selectedOrderIdLive.value = orderId
        Timber.i("Delivery order selected $orderId")
    }

    fun setSelectOrder(order: DeliveryOrder) {
        setSelectOrder(order.orderId)
    }

    fun getSelectedOrder() = selectedOrderLive.value

    fun getSelectedOrderLive() = selectedOrderLive

    fun getDeliveryOrdersLive() = deliveryOrdersLive

    fun acceptOrder(order: DeliveryOrder) {
        loadingOrderAccepting.postValue(true)
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

            loadingOrderAccepting.postValue(false)
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
}

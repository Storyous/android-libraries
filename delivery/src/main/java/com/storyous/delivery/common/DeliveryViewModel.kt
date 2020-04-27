package com.storyous.delivery.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.onNonNull
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryRepository
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

        const val MESSAGE_ERROR_STATE_CONFLICT = 1
        const val MESSAGE_ERROR_OTHER = 2
        const val MESSAGE_OK_DECLINED = 3
    }

    private val selectedOrderIdLive = MutableLiveData<String>(null)
    private val selectedOrderLive = Transformations.switchMap(selectedOrderIdLive) {
        DeliveryConfiguration.deliveryRepository?.getOrderLive(it)
    }
    private val deliveryOrdersLive: LiveData<List<DeliveryOrder>> = MediatorLiveData<List<DeliveryOrder>>().apply {
        addSource(DeliveryConfiguration.deliveryRepository!!.deliveryOrdersLive) { orders ->
            if (getSelectedOrder()?.orderId?.let { orderId -> orders.find { it.orderId == orderId } } == null) {
                deselectOrder()
            }
            value = orders
        }
    }

    val loadingOrderAccepting = MutableLiveData(false)
    val loadingOrderCancelling = MutableLiveData(false)
    val loadingOrderDispatching = MutableLiveData(false)
    val messagesToShow = MutableLiveData(mutableListOf<Int>())

    fun loadOrders() {
        launch(provider.Main) {
            DeliveryConfiguration.placeInfo?.let {
                DeliveryConfiguration.deliveryRepository?.loadDeliveryOrders(
                    it.merchantId,
                    it.placeId
                )
            }
        }
    }

    fun deselectOrder() {
        selectedOrderIdLive.value = null
    }

    fun setSelectOrder(orderId: String) {
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

            val result = withContext(provider.IO) {
                onNonNull(
                    DeliveryConfiguration.placeInfo?.merchantId,
                    DeliveryConfiguration.placeInfo?.placeId
                ) { merchantId, placeId ->
                    DeliveryConfiguration.deliveryRepository
                        ?.acceptDeliveryOrder(merchantId, placeId, order)
                }
            }

            when (result) {
                DeliveryRepository.RESULT_OK -> {
                    DeliveryConfiguration.deliveryRepository?.addConfirmedOrder(order)
                }
                DeliveryRepository.RESULT_ERR_CONFLICT -> {
                    addMessageToShow(MESSAGE_ERROR_STATE_CONFLICT)
                }
                else -> addMessageToShow(MESSAGE_ERROR_OTHER)
            }

            loadingOrderAccepting.postValue(false)
        }
    }

    fun onCancelOrderClicked(reason: String) {
        loadingOrderCancelling.postValue(true)
        getSelectedOrder()?.let { selected ->
            launch(provider.Main) {
                val result = withContext(provider.IO) {
                    onNonNull(
                        DeliveryConfiguration.placeInfo?.merchantId,
                        DeliveryConfiguration.placeInfo?.placeId
                    ) { merchantId, placeId ->
                        DeliveryConfiguration.deliveryRepository
                            ?.cancelDeliveryOrder(merchantId, placeId, selected, reason)
                    }
                }

                when (result) {
                    DeliveryRepository.RESULT_OK -> {
                        addMessageToShow(MESSAGE_OK_DECLINED)
                    }
                    DeliveryRepository.RESULT_ERR_CONFLICT -> {
                        addMessageToShow(MESSAGE_ERROR_STATE_CONFLICT)
                    }
                    else -> addMessageToShow(MESSAGE_ERROR_OTHER)
                }

                loadingOrderCancelling.postValue(false)
            }
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

            val result = withContext(provider.IO) {
                onNonNull(
                    DeliveryConfiguration.placeInfo?.merchantId,
                    DeliveryConfiguration.placeInfo?.placeId
                ) { merchantId, placeId ->
                    DeliveryConfiguration.deliveryRepository
                        ?.notifyDeliveryOrderDispatched(merchantId, placeId, order)
                }
            }

            when (result) {
                DeliveryRepository.RESULT_OK -> {
                    // do nothing
                }
                DeliveryRepository.RESULT_ERR_CONFLICT -> {
                    addMessageToShow(MESSAGE_ERROR_STATE_CONFLICT)
                }
                else -> addMessageToShow(MESSAGE_ERROR_OTHER)
            }

            loadingOrderDispatching.postValue(false)
        }
    }
}

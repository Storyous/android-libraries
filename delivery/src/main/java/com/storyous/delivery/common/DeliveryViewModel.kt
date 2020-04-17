package com.storyous.delivery.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.onNonNull
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DeliveryViewModel @JvmOverloads constructor(
    application: Application,
    val deliveryResourceProvider: IDeliveryResourceProvider = (application as IDeliveryApplication).deliveryResourceProvider
) : AndroidViewModel(application), CoroutineScope by CoroutineProviderScope() {

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

        const val MAX_PS_NAME_LENGTH = 20
    }

    private val selectedOrderLive = MutableLiveData<DeliveryOrder>()
    private val deliveryModel = deliveryResourceProvider.deliveryModel

    val loadingOrderAccepting = MutableLiveData(false)
    val loadingOrderCancelling = MutableLiveData(false)
    val messagesToShow = MutableLiveData(mutableListOf<Int>())

    fun deselectOrder() {
        selectedOrderLive.value = null
    }

    fun setSelectOrder(orderId: String) {
        launch(provider.Main) {
            deliveryResourceProvider.deliveryRepository.findOrder(orderId)
                ?.also { setSelectOrder(it) }
        }
    }

    fun setSelectOrder(order: DeliveryOrder) {
        selectedOrderLive.value = order
        Timber.i("Delivery order selected ${order.orderId}")
    }

    fun getSelectedOrder() = selectedOrderLive.value

    fun getSelectedOrderLive() = selectedOrderLive

    fun getDeliveryOrdersLive() = deliveryResourceProvider.deliveryRepository.getDeliveryOrders()

    fun acceptOrder(order: DeliveryOrder) {
        loadingOrderAccepting.postValue(true)
        // hold data of selected order to prevent change data when is selected different order
        launch(provider.Main) {

            val result = withContext(provider.IO) {
                onNonNull(
                    deliveryResourceProvider.getMerchantId(),
                    deliveryResourceProvider.getPlaceId()
                ) { merchantId, placeId ->
                    deliveryResourceProvider.deliveryRepository.acceptDeliveryOrder(
                        merchantId,
                        placeId,
                        order
                    )
                }
            }

            when (result) {
                DeliveryRepository.RESULT_OK -> {
                    deliveryModel.onConfirmResultOk(order)
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
                        deliveryResourceProvider.getMerchantId(),
                        deliveryResourceProvider.getPlaceId()
                    ) { merchantId, placeId ->
                        deliveryResourceProvider.deliveryRepository.cancelDeliveryOrder(
                            merchantId, placeId, selected, reason
                        )
                    }
                }

                when (result) {
                    DeliveryRepository.RESULT_OK -> addMessageToShow(MESSAGE_OK_DECLINED)
                    DeliveryRepository.RESULT_ERR_CONFLICT -> addMessageToShow(
                        MESSAGE_ERROR_STATE_CONFLICT
                    )
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
        deliveryResourceProvider.deliveryRepository.ringingState.value = false
    }
}

package com.storyous.delivery.common

import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Suppress("TooManyFunctions")
open class DeliveryModel : CoroutineScope by CoroutineProviderScope() {

    private var loadingOrdersJob: Job? = null

    var newOrdersInterceptor: suspend (List<DeliveryOrder>) -> Unit = { orders ->
        orders.forEach {
            if (it.autoConfirm == true && DeliveryConfiguration.placeInfo?.autoConfirmEnabled == true) {
                confirmOrder(it)
            }
        }
    }

    companion object {
        fun getOrderInfo(provider: String, getString: (Int, String?) -> String): String {
            val type = when (provider) {
                DeliveryViewModel.PROVIDER_DJ -> getString(
                    R.string.delivery_service_damejidlo,
                    null
                )
                DeliveryViewModel.PROVIDER_UBER_EATS -> getString(
                    R.string.delivery_service_ubereats,
                    null
                )
                DeliveryViewModel.PROVIDER_DELIVERECT -> getString(
                    R.string.delivery_service_deliverect,
                    null
                )
                DeliveryViewModel.PROVIDER_PHONE -> getString(R.string.delivery_service_phone, null)
                DeliveryViewModel.PROVIDER_STORYOUS_TAKEAWAY_WEB,
                DeliveryViewModel.PROVIDER_STORYOUS_TAKEAWAY_APP ->
                    getString(R.string.delivery_service_storyous_takeaway, null)
                else -> null
            }
            return getString(R.string.delivery_service_info, type)

            /* we will use only provider name until we have correct order number.
               Then we will add "No.:" to string delivery_service_info and this:
            return if (type == null) {
                getString(R.string.delivery_service_info_unknown, null)
            } else {
                getString(R.string.delivery_service_info, type)
            }
             */
        }
    }

    fun loadOrders(): Job? {
        if (loadingOrdersJob?.isActive != true) {
            loadingOrdersJob = launch(provider.Main) {
                DeliveryConfiguration.placeInfo?.let {
                    Timber.d("Delivery order download started.")

                    DeliveryConfiguration.deliveryRepository
                        ?.loadDeliveryOrders(it.merchantId, it.placeId)

                    DeliveryConfiguration.deliveryRepository
                        ?.getNewOrdersFromDb()
                        ?.also { newOrdersInterceptor(it) }

                } ?: Timber.w("Delivery is not configured to load new orders")
            }
        }
        return loadingOrdersJob
    }


    suspend fun confirmOrder(order: DeliveryOrder) {
        val (placeId, merchantId) = DeliveryConfiguration.placeInfo ?: return

        val result = withContext(provider.IO) {
            DeliveryConfiguration.deliveryRepository
                ?.acceptDeliveryOrder(merchantId, placeId, order)
        }

        if (DeliveryRepository.RESULT_OK == result) {
            DeliveryConfiguration.deliveryRepository?.addConfirmedOrder(order)
        }
    }

    suspend fun decline(order: DeliveryOrder) {
        val (placeId, merchantId) = DeliveryConfiguration.placeInfo ?: return

        withContext(provider.IO) {
            DeliveryConfiguration.deliveryRepository
                ?.cancelDeliveryOrder(merchantId, placeId, order, "Unknown desk")
        }
    }

    fun clear() = launch {
        DeliveryConfiguration.deliveryRepository?.clear()
    }
}

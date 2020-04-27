package com.storyous.delivery.common

import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.onNonNull
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("TooManyFunctions")
open class DeliveryModel : CoroutineScope by CoroutineProviderScope() {

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

    fun confirmAutoConfirmOrders() {
        if (DeliveryConfiguration.placeInfo?.autoConfirm == true) {
            DeliveryConfiguration.deliveryRepository?.deliveryOrdersLive?.value
                ?.filter { it.state == DeliveryOrder.STATE_NEW && it.autoConfirm == true }
                ?.forEach { confirmAutoConfirmOrder(it) }
        }
    }

    private fun confirmAutoConfirmOrder(order: DeliveryOrder) {
        launch(provider.Main) {
            val result = withContext(provider.IO) {
                onNonNull(
                    DeliveryConfiguration.placeInfo?.merchantId,
                    DeliveryConfiguration.placeInfo?.placeId
                ) { merchantId, placeId ->
                    DeliveryConfiguration.deliveryRepository?.acceptDeliveryOrder(
                        merchantId,
                        placeId,
                        order
                    )
                }
            }

            if (DeliveryRepository.RESULT_OK == result) {
                DeliveryConfiguration.deliveryRepository?.addConfirmedOrder(order)
            }
        }
    }
}

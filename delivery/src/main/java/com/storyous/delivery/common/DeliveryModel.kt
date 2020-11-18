package com.storyous.delivery.common

import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryException
import com.storyous.delivery.common.repositories.ERR_NO_AUTH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Suppress("TooManyFunctions")
open class DeliveryModel : CoroutineScope by CoroutineProviderScope() {

    private var loadingOrdersJob: Job? = null
    private var alreadyConfirmedOrderIds: List<String>? = null

    var newOrdersInterceptor: suspend (List<DeliveryOrder>) -> Unit = { orders ->
        orders.forEach {
            if (it.autoConfirm == true && DeliveryConfiguration.placeInfo?.autoConfirmEnabled == true) {
                runCatching { confirm(it) }
            }
        }
    }

    var dispatchedOrderInterceptor: suspend (DeliveryOrder) -> Unit = {}

    var confirmedOrderInterceptor: suspend (DeliveryOrder) -> Unit = {}

    companion object {
        fun getOrderInfo(provider: String, getString: (Int, String?) -> String): String {
            val type = when (provider) {
                DeliveryViewModel.PROVIDER_DJ ->
                    getString(R.string.delivery_service_damejidlo, null)
                DeliveryViewModel.PROVIDER_UBER_EATS ->
                    getString(R.string.delivery_service_ubereats, null)
                DeliveryViewModel.PROVIDER_DELIVERECT ->
                    getString(R.string.delivery_service_deliverect, null)
                DeliveryViewModel.PROVIDER_PHONE ->
                    getString(R.string.delivery_service_phone, null)
                DeliveryViewModel.PROVIDER_STORYOUS_TAKEAWAY_WEB,
                DeliveryViewModel.PROVIDER_STORYOUS_TAKEAWAY_APP ->
                    getString(R.string.delivery_service_storyous_takeaway, null)
                DeliveryViewModel.PROVIDER_STORYOUS_ONEMENU_QR ->
                    getString(R.string.delivery_service_onemenu_qr, null)
                else -> null
            }
            return type?.let { getString(R.string.delivery_service_info, it) } ?: ""

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

    fun loadOrders(): Job {
        if (loadingOrdersJob?.isActive != true) {
            loadingOrdersJob = launch(provider.Main) {
                val (placeId, merchantId, autoConfirmEnabled) = DeliveryConfiguration.placeInfo.also {
                    if (it == null) Timber.w("Delivery is not configured to load new orders")
                } ?: return@launch

                Timber.d("Delivery order download started.")

                alreadyConfirmedOrderIds = DeliveryConfiguration.deliveryRepository
                    .takeIf { autoConfirmEnabled }
                    ?.getConfirmedOrdersFromDb()
                    ?.map { it.orderId }

                DeliveryConfiguration.deliveryRepository
                    ?.loadDeliveryOrders(merchantId, placeId)
                    ?.takeIf { autoConfirmEnabled }
                    ?.data?.filter {
                        it.state == DeliveryOrder.STATE_CONFIRMED &&
                            alreadyConfirmedOrderIds?.contains(it.orderId) != true
                    }?.forEach { confirmedOrderInterceptor(it) }

                DeliveryConfiguration.deliveryRepository
                    ?.getNewOrdersFromDb()
                    ?.also { newOrdersInterceptor(it) }
            }
        }
        return loadingOrdersJob!!
    }

    @Throws(DeliveryException::class)
    suspend fun confirm(order: DeliveryOrder): DeliveryOrder? {
        val (placeId, merchantId) = DeliveryConfiguration.placeInfo
            ?: throw DeliveryException(ERR_NO_AUTH)

        return withContext(provider.IO) {
            DeliveryConfiguration.deliveryRepository
                ?.confirmDeliveryOrder(merchantId, placeId, order)
        }?.also {
            if (it.state == DeliveryOrder.STATE_CONFIRMED) {
                alreadyConfirmedOrderIds = alreadyConfirmedOrderIds?.plus(it.orderId)
                confirmedOrderInterceptor(it)
            }
        }
    }

    @Throws(DeliveryException::class)
    suspend fun decline(order: DeliveryOrder, reason: String): DeliveryOrder? {
        val (placeId, merchantId) = DeliveryConfiguration.placeInfo
            ?: throw DeliveryException(ERR_NO_AUTH)

        return withContext(provider.IO) {
            DeliveryConfiguration.deliveryRepository
                ?.declineDeliveryOrder(merchantId, placeId, order, reason)
        }
    }

    @Throws(DeliveryException::class)
    suspend fun dispatch(order: DeliveryOrder): DeliveryOrder? {
        val (placeId, merchantId) = DeliveryConfiguration.placeInfo
            ?: throw DeliveryException(ERR_NO_AUTH)

        return withContext(provider.IO) {
            DeliveryConfiguration.deliveryRepository
                ?.dispatchDeliveryOrder(merchantId, placeId, order)
                ?.also { dispatchedOrderInterceptor(it) }
        }
    }

    fun clear() = launch {
        DeliveryConfiguration.deliveryRepository?.clear()
    }
}

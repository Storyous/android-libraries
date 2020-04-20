package com.storyous.delivery.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.storyous.commonutils.CoroutineProviderScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class DownloadDeliveryReceiver : BroadcastReceiver(), CoroutineScope by CoroutineProviderScope() {

    override fun onReceive(context: Context, intent: Intent) {
        val merchantId = Configuration.placeInfo?.merchantId
        val placeId = Configuration.placeInfo?.placeId

        if (merchantId == null || placeId == null) {
            Timber.w("Unknown merchantId or placeId to load delivery orders.")
        } else {
            Timber.d("Delivery order download started.")
            launch {
                Configuration.deliveryRepository?.loadDeliveryOrders(merchantId, placeId)
                Configuration.deliveryModel.confirmAutoConfirmOrders()
            }
        }
    }
}

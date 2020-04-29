package com.storyous.delivery.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.storyous.commonutils.CoroutineProviderScope
import kotlinx.coroutines.CoroutineScope

class DownloadDeliveryReceiver : BroadcastReceiver(), CoroutineScope by CoroutineProviderScope() {

    override fun onReceive(context: Context, intent: Intent) {
        DeliveryConfiguration.deliveryModel.loadOrders()
    }
}

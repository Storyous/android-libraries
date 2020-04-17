package com.storyous.delivery.common

import android.content.Context

internal val Context.deliveryResourceProvider
    get() = (applicationContext as IDeliveryApplication).deliveryResourceProvider

package com.storyous.roottools

import android.content.Context
import com.storyous.roottools.magisk.MagiskDetector

object RootTools {

    suspend fun isMagiskPresent(ctx: Context) = MagiskDetector(ctx).isMagiskPresent

    suspend fun isRooted(ctx: Context) = isMagiskPresent(ctx)

}

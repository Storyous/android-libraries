package com.storyous.commonutils.terminal

import android.app.Activity
import android.content.Intent

interface ActivityResultObserver {
    fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    )
}

object ActivityResult : ActivityResultObserver {

    val observers = mutableListOf<ActivityResultObserver>()

    fun observe(observer: ActivityResultObserver) {
        synchronized(observers) {
            observers.add(observer)
        }
    }

    fun removeObserver(observer: ActivityResultObserver) {
        synchronized(observers) {
            observers.remove(observer)
        }
    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        synchronized(observers) {
            observers.forEach {
                it.onActivityResult(activity, requestCode, resultCode, data)
            }
        }
    }
}

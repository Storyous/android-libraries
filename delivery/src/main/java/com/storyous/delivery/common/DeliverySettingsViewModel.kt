package com.storyous.delivery.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.common.api.DeliverySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliverySettingsViewModel : ViewModel(), CoroutineScope by CoroutineProviderScope() {

    companion object {
        private const val DUMMY_DELAY = 1000L
        private const val DUMMY_PREP_TIME = 30
    }

    val settings = MutableLiveData<DeliverySettings>()
    val progress = MutableLiveData<Boolean>()

    fun loadSettings() {
        launch {
            progress.value = true
            withContext(provider.IO) {
                delay(DUMMY_DELAY)
            }
            settings.value = DeliverySettings(true, DUMMY_PREP_TIME, DeliverySettings.VALUE_ENABLED)
            progress.value = false
        }
    }

    fun saveSettings(newSettings: DeliverySettings) {
        launch {
            progress.value = true
            withContext(provider.IO) {
                delay(DUMMY_DELAY)
            }
            settings.value = newSettings
            progress.value = false
        }
    }
}

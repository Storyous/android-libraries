package com.storyous.commonutils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.annotation.IntRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KClass

object AlarmUtils : CoroutineScope by CoroutineProviderScope() {

    const val MIN_INTERVAL = 60 * 1000L
    const val START_NOW = 0L

    private const val SP_REPEATING_ALARMS = "repeatingAlarms"
    private const val WAKE_UP_INTERVAL = MIN_INTERVAL

    private fun getSP(context: Context) =
        context.getSharedPreferences(SP_REPEATING_ALARMS, Activity.MODE_PRIVATE)

    fun init(context: Context) {
        getSP(context).all
            .filterValues { it is Long && it > 0 }
            .map { Class.forName(it.key).kotlin as KClass<out BroadcastReceiver> to it.value as Long }
            .forEach { setRepeatingAlarm(context, it.first, START_NOW, it.second) }
    }

    fun doNotKeepWakeUp(context: Context) {
        removeRepeatingAlarm(context, WakeUpReceiver::class)
    }

    @JvmOverloads
    fun keepWakeUp(
        context: Context,
        @IntRange(from = MIN_INTERVAL) interval: Long = WAKE_UP_INTERVAL
    ) {
        enableStartAlarmOnBoot(context)
        setRepeatingAlarm(context, WakeUpReceiver::class, START_NOW, interval)
    }

    private fun createPendingIntent(
        context: Context,
        clazz: KClass<out BroadcastReceiver>
    ) = PendingIntent.getBroadcast(context, 0, Intent(context, clazz.java), 0)

    fun removeRepeatingAlarm(context: Context, clazz: KClass<out BroadcastReceiver>) =
        launch(provider.IO) {
            getSP(context).edit().remove(clazz.qualifiedName).apply()

            context.getAlarmManager().cancel(createPendingIntent(context, clazz))
        }

    fun setRepeatingAlarm(
        context: Context,
        clazz: KClass<out BroadcastReceiver>,
        startIn: Long,
        @IntRange(from = MIN_INTERVAL) interval: Long
    ) = launch(provider.IO) {
        getSP(context).edit().putLong(clazz.qualifiedName, interval).apply()

        context.getAlarmManager().setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + startIn,
            interval,
            createPendingIntent(context, clazz)
        )
    }

    private fun enableStartAlarmOnBoot(context: Context) {
        val receiver = ComponentName(context, InitAlarmOnBootReceiver::class.java)

        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

fun Context.getAlarmManager() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

class InitAlarmOnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmUtils.init(context)
        }
    }
}

class WakeUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("App woke up.")
    }
}

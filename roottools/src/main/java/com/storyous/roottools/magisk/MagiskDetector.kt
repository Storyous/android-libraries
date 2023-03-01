package com.storyous.roottools.magisk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal interface MagiskDetector : IIsolatedService {

    companion object {

        private val mutex = Mutex()
        @Volatile
        private var instance: MagiskDetector? = null

        private suspend fun getIsolatedService(
            ctx: Context
        ): IIsolatedService? = suspendCoroutine { cont ->
            val connection = object : ServiceConnection {

                private var resumed = false

                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    if (!resumed) {
                        resumed = true
                        cont.resume(IIsolatedService.Stub.asInterface(service))
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    if (instance != null) {
                        instance = null
                        ctx.unbindService(this)
                    }
                }
            }

            ctx.bindService(
                Intent(ctx, IsolatedService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            ).also {
                if (!it) {
                    cont.resumeWithException(IllegalStateException("Failed IsolatedService binding"))
                }
            }
        }

        suspend operator fun invoke(ctx: Context): MagiskDetector = instance ?: mutex.withLock {
            instance ?: MagiskDetektorImpl(
                getIsolatedService(ctx.applicationContext)
                    ?: throw IllegalStateException("IIsolatedService not found")
            ).also { instance = it }
        }

    }
}

private class MagiskDetektorImpl(
    service: IIsolatedService
) : MagiskDetector, IIsolatedService by service

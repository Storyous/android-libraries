package com.storyous.roottools.magisk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal interface MagiskDetector : IIsolatedService {

    companion object {

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
                    //cont.resumeWithException(IllegalStateException("${name.className} disconnected"))
                }
            }

            ctx.applicationContext.bindService(
                Intent(ctx, IsolatedService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            ).also {
                if (!it) {
                    cont.resumeWithException(IllegalStateException("Failed IsolatedService binding"))
                }
            }
        }

        suspend operator fun invoke(ctx: Context): MagiskDetector {
            return getIsolatedService(ctx)
                ?.let { MagiskDetektorImpl(it) }
                ?: throw IllegalStateException("IIsolatedService not found")
        }
    }
}

private class MagiskDetektorImpl(
    service: IIsolatedService
) : MagiskDetector, IIsolatedService by service

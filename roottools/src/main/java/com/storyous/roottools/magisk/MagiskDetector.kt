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
        private suspend fun getIsolatedService(ctx: Context): IIsolatedService? = suspendCoroutine {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    it.resume(IIsolatedService.Stub.asInterface(service))
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    it.resumeWithException(IllegalStateException("${name.className} disconnected"))
                }
            }

            ctx.applicationContext.bindService(
                Intent(ctx, IsolatedService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
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

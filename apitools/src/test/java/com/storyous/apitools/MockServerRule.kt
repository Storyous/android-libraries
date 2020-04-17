package com.storyous.apitools

import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockServerRule(
    val port: Int = 0,
    private val onServerStart: (String) -> Unit
) : TestWatcher() {

    val server = MockWebServer()

    override fun starting(description: Description?) {
        super.starting(description)

        startServer()
    }

    fun startServer() {
        server.start(port)
        onServerStart(getServerUrl())
    }

    private fun getServerUrl(): String {
        return server.url("/").toString()
    }

    override fun finished(description: Description?) {
        super.finished(description)

        stopServer()
    }

    fun stopServer() {
        server.shutdown()
        server.close()
    }
}

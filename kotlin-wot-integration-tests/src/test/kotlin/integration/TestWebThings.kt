package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.credentials.BearerCredentials
import ai.ancf.lmos.wot.security.BearerSecurityScheme
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import ai.ancf.lmos.wot.thing.schema.genericWriteProperty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TestWebThings {

    @Test
    fun `Should control devices`() = runTest {
        val http = HttpProtocolClientFactory()
        val https = HttpsProtocolClientFactory()
        val servient = Servient(clientFactories = listOf(http, https), credentialStore =
            mapOf("https://plugfest.webthings.io" to
                BearerCredentials("eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjE1OWM4MzhlLWYxZmItNGE3ZC1iZDY2LTBlNmI1ZDZjNWVhMCJ9.eyJyb2xlIjoidXNlcl90b2tlbiIsImlhdCI6MTczMjI5MjczNSwiaXNzIjoiaHR0cHM6Ly9wbHVnZmVzdC53ZWJ0aGluZ3MuaW8ifQ.CpQ5MLSygmCJFS6yz4Xdf0xyImwqBWvNfKNZPX9DNHjyjuq5wzq0mWurSu11wR-BwnZ2lnFcIId3ytfbo9hBwg")
            ))

        val wot = Wot.create(servient)

        val thingDescription = wot.requestThingDescription("https://plugfest.webthings.io/things/virtual-things-2",
            BearerSecurityScheme())

        val testThing = wot.consume(thingDescription)
        val status = testThing.genericReadProperty<Boolean>("on")

        println(status)

        testThing.genericWriteProperty("level", 50)
        testThing.genericWriteProperty("on", true)


    }
}
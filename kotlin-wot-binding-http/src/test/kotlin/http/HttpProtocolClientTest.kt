package ai.ancf.lmos.wot.binding.http
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.security.BearerSecurityScheme
import ai.ancf.lmos.wot.security.NoSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClientException
import com.github.tomakehurst.wiremock.WireMockServer
import com.marcinziolo.kotlin.wiremock.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class HttpProtocolClientTest {

    private lateinit var form: Form
    private lateinit var securityScheme: SecurityScheme

    private val wiremock: WireMockServer = WireMockServer(8080)

    @BeforeTest
    fun setUp() {
        wiremock.start()
        form = mockk()
        securityScheme = mockk()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun readResourceCreatesProperRequest() = runTest {
        wiremock.get {
            url equalTo "/foo"
        } returns {
            statusCode = 200
        }

        val form = Form("${wiremock.baseUrl()}/foo")

        val client = HttpProtocolClient()
        client.readResource(form)

        wiremock.verify {
            url equalTo "/foo"
            exactly = 1
        }
    }

    @Test
    fun writeResourceCreatesProperRequest() = runTest {
        wiremock.put {
            url equalTo "/foo"
            body equalTo  """{"key": "value"}"""
        } returns {
            statusCode = 200
        }

        val form = Form("${wiremock.baseUrl()}/foo")
        val jsonContent = """{"key": "value"}"""
        val content = Content("application/json", jsonContent.toByteArray())

        val client = HttpProtocolClient()
        client.writeResource(form, content)

        wiremock.verify {
            url equalTo "/foo"
            exactly = 1
        }
    }

    @Test
    fun invokeResourceCreatesProperRequest() = runTest {
        wiremock.post {
            url equalTo "/foo"
            body equalTo  """{"key": "value"}"""
        } returns {
            statusCode = 200
        }

        val form = Form("${wiremock.baseUrl()}/foo")
        val jsonContent = """{"key": "value"}"""
        val content = Content("application/json", jsonContent.toByteArray())

        val client = HttpProtocolClient()
        client.invokeResource(form, content)

        wiremock.verify {
            url equalTo "/foo"
            exactly = 1
        }
    }

    @Test
    fun subscribeResourceHandlesLongPolling() = runTest {
        wiremock.get {
            url equalTo "/foo"
        } returns {
            statusCode = 200
        }

        every { form.href } returns "${wiremock.baseUrl()}/foo"

        val client = HttpProtocolClient()
        client.subscribeResource(form)

    }

    @Test
    fun resolveRequestToContentThrowsExceptionForInvalidResponse() = runTest {
        wiremock.get {
            url equalTo "/foo"
        } returns {
            statusCode = 500
        }

        every { form.href } returns "${wiremock.baseUrl()}/foo"

        val client = HttpProtocolClient()

        assertFailsWith<ProtocolClientException> {
            client.readResource(form)
        }
    }
}
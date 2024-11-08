package ai.ancf.lmos.wot.binding.http
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.form.Form
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.apache.http.StatusLine
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpProtocolClientTest {

    private lateinit var form: Form
    private lateinit var statusLine: StatusLine
    private lateinit var securityScheme: SecurityScheme
    private lateinit var httpClient: HttpClient


    @BeforeTest
    fun setUp() {
        form = mockk()
        statusLine = mockk()
        securityScheme = mockk()
        httpClient = mockk()
    }

    @Test
    fun `readResource should create proper request`(): Unit = runTest {
        every { form.href ?: "" } returns "http://localhost/foo"
        every { statusLine.statusCode } returns 200 // HTTP Status OK

        val response : HttpResponse = mockk()

        coEvery { httpClient.request("http://localhost/foo", any()) } returns response

        val client = HttpProtocolClient(httpClient)
        client.readResource(form)
    }
}
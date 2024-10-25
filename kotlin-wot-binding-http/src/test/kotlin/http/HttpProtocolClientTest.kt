package ai.ancf.lmos.wot.binding.http
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.form.Form
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.apache.http.StatusLine
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpProtocolClientTest {

    private var form: Form? = null
    private var statusLine: StatusLine? = null
    private var securityScheme: SecurityScheme? = null

    @BeforeTest
    fun setUp() {
        form = mockk()
        statusLine = mockk()
        securityScheme = mockk()
    }

    @Test
    fun `readResource should create proper request`() = runBlocking {
        every { form?.href ?: "" } returns "http://localhost/foo"
        every { statusLine?.statusCode } returns 200 // HTTP Status OK

        val client = HttpProtocolClient()
        client.readResource(form!!)


    }
}
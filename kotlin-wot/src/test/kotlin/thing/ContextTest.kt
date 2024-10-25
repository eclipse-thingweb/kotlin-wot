package ai.ancf.lmos.wot.thing


import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class ContextTest {

    private val jsonMapper = ObjectMapper()

    @Test
    @Throws(IOException::class)
    fun fromJson() {
        // single value
        assertEquals(
            Context("http://www.w3.org/ns/td"),
            jsonMapper.readValue<Context>(
                "\"http://www.w3.org/ns/td\"",
                Context::class.java
            )
        )

        // array
        assertEquals(
            Context("http://www.w3.org/ns/td"),
            jsonMapper.readValue(
                "[\"http://www.w3.org/ns/td\"]",
                Context::class.java
            )
        )

        // multi type array
        assertEquals(
            Context("http://www.w3.org/ns/td")
                .addContext("saref", "https://w3id.org/saref#"),
            jsonMapper.readValue(
                "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
                Context::class.java
            )
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun toJson() {
        // single value
        assertEquals(
            "\"http://www.w3.org/ns/td\"",
            jsonMapper.writeValueAsString(Context("http://www.w3.org/ns/td"))
        )

        // multi type array
        assertEquals(
            "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
            jsonMapper.writeValueAsString(
                Context("http://www.w3.org/ns/td")
                    .addContext("saref", "https://w3id.org/saref#")
            )
        )
    }
}
package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypeTest {
    private val jsonMapper = ObjectMapper()
    @Test
    @Throws(IOException::class)
    fun fromJson() {
        // single value
        assertEquals(
            Type("Thing"),
            jsonMapper.readValue("\"Thing\"", Type::class.java)
        )

        // array
        assertEquals(
            Type("Thing").addType("saref:LightSwitch"),
            jsonMapper.readValue("[\"Thing\",\"saref:LightSwitch\"]", Type::class.java)
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun toJson() {
        // single value
        assertEquals(
            "\"Thing\"",
            jsonMapper.writeValueAsString(Type("Thing"))
        )

        // multi type array
        JsonAssertions.assertThatJson(jsonMapper.writeValueAsString(Type("Thing").addType("saref:LightSwitch")))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isArray()
            .contains("Thing", "saref:LightSwitch")
    }
}
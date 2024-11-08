package ai.ancf.lmos.wot.thing.property


import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.schema.IntProperty
import ai.ancf.lmos.wot.thing.schema.StringProperty
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThingPropertyTest {
    @Test
    fun testEquals() {
        val property1 = StringProperty().apply { title = "title" }
        val property2 = StringProperty().apply { title = "title" }
        assertEquals(property1, property2)
    }

    @Test
    fun testHashCode() {
        val property1 = StringProperty().apply { title = "title" }.hashCode()
        val property2 = StringProperty().apply { title = "title" }.hashCode()
        assertEquals(property1, property2)
    }

    @Test
    fun toJson() {
        val property = IntProperty().apply {
            objectType=Type("saref:Temperature")
            description = "bla bla"
            observable=true
            readOnly=true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Temperature","description":"bla bla","type":"integer","observable":true,"readOnly":true}"""
            )
    }

    @Test
    fun fromJson() {
        val json = """{"@type":"saref:Temperature","description":"bla bla","type":"string","observable":true,"readOnly":true}"""

        val parsedProperty = JsonMapper.instance.readValue<StringProperty>(json)
        val property = StringProperty().apply {
            objectType = Type("saref:Temperature")
            description = "bla bla"
            observable = true
            readOnly = true
            minLength = 10
        }
        assertEquals(property, parsedProperty)
    }

    @Test
    fun testConstructor() {
        val property = IntProperty().apply {
            objectType=Type("saref:Temperature")
            observable=true
            readOnly=true
            writeOnly=false
        }
        assertEquals("saref:Temperature", property.objectType?.types?.first())
        assertTrue(property.observable)
        assertTrue(property.readOnly)
        assertFalse(property.writeOnly)
    }

}
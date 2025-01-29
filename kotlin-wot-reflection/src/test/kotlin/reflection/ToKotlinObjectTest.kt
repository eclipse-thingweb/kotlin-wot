package ai.ancf.lmos.wot.reflection

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder.toKotlinObject
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ToKotlinObjectTests {

    private val objectMapper = JsonMapper.instance

    @Test
    fun testPrimitiveString() {
        val jsonString = "\"Hello\""
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<String>())
        assertEquals("Hello", result)
    }

    @Test
    fun testPrimitiveInt() {
        val jsonString = "42"
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<Int>())
        assertEquals(42, result)
    }

    @Test
    fun testPrimitiveBoolean() {
        val jsonString = "true"
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<Boolean>())
        assertEquals(true, result)
    }

    @Test
    fun testListOfPrimitives() {
        val jsonString = "[1, 2, 3]"
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<List<Int>>())
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testNestedDataClass() {
        val jsonString = """
        {
            "title": "Nested Example",
            "details": {
                "name": "John",
                "age": 30
            }
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<NestedExample>())
        val expected = NestedExample(title = "Nested Example", details = SimpleExample(name = "John", age = 30))
        assertEquals(expected, result)
    }

    @Test
    fun testListOfNestedDataClasses() {
        val jsonString = """
        {
            "items": [10, 20, 30]
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<ListExample>())
        val expected = ListExample(items = listOf(10, 20, 30))
        assertEquals(expected, result)
    }

    @Test
    fun testMapExample() {
        val jsonString = """
        {
            "properties": {
                "key1": 1.1,
                "key2": 2.2
            }
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<MapExample>())
        val expected = MapExample(properties = mapOf("key1" to 1.1, "key2" to 2.2))
        assertEquals(expected, result)
    }

    @Test
    fun testListOfMaps() {
        val jsonString = """
        {
            "values": [
                {"key1": 1},
                {"key2": 2}
            ]
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<MixedExample>())
        val expected = MixedExample(values = listOf(mapOf("key1" to 1), mapOf("key2" to 2)))
        assertEquals(expected, result)
    }

    @Test
    fun testFloatInDataClass() {
        val jsonString = """
        {
            "value": 3.14
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<FloatExample>())
        val expected = FloatExample(value = 3.14f)
        assertEquals(expected, result)
    }

    @Test
    fun testDoubleInDataClass() {
        val jsonString = """
        {
            "value": 3.14159
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<DoubleExample>())
        val expected = DoubleExample(value = 3.14159)
        assertEquals(expected, result)
    }

    @Test
    fun testNumberInDataClass() {
        val jsonString = """
        {
            "value": 42
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<NumberExample>())
        val expected = NumberExample(value = 42)
        assertEquals(expected, result)
    }

    @Test
    fun testBooleanInDataClass() {
        val jsonString = """
        {
            "value": true
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<BooleanExample>())
        val expected = BooleanExample(value = true)
        assertEquals(expected, result)
    }

    @Test
    fun testListOfFloat() {
        val jsonString = "[3.14, 2.71, 1.61]"
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<List<Float>>()) as List<Float>
        assertThat(result).containsExactly(3.14f, 2.71f, 1.61f)
    }

    @Test
    fun testListOfDouble() {
        val jsonString = "[3.14159, 2.71828, 1.61803]"
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<List<Double>>())
        assertEquals(listOf(3.14159, 2.71828, 1.61803), result)
    }

    @Test
    fun testMixedTypesExample() {
        val jsonString = """
        {
            "floatValue": 3.14,
            "doubleValue": 3.14159,
            "numberValue": 42,
            "booleanValue": true
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<MixedTypesExample>())
        val expected = MixedTypesExample(
            floatValue = 3.14f,
            doubleValue = 3.14159,
            numberValue = 42,
            booleanValue = true
        )
        assertEquals(expected, result)
    }

    @Test
    fun testEnumConversion() {
        val jsonString = "\"ACTIVE\""
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<Status>())
        assertEquals(Status.ACTIVE, result)
    }

    @Test
    fun testEnumInDataClass() {
        val jsonString = """
        {
            "username": "john_doe",
            "status": "PENDING"
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<User>())
        val expected = User(username = "john_doe", status = Status.PENDING)
        assertEquals(expected, result)
    }

    @Test
    fun testEnumConversionWithInvalidValue() {
        val jsonString = "\"UNKNOWN\""
        val input: JsonNode = objectMapper.readTree(jsonString)
        assertFailsWith<IllegalArgumentException> {
            toKotlinObject(input, typeOf<Status>())
        }
    }

    @Test
    fun testListOfEnums() {
        val jsonString = "[\"ACTIVE\", \"INACTIVE\", \"PENDING\"]"
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<List<Status>>()) as List<Status>

        //val result = objectMapper.treeToValue<List<Status>>(input)
        assertThat(result).containsExactly(Status.ACTIVE, Status.INACTIVE, Status.PENDING)
    }

    @Test
    fun testMapOfEnums() {
        val jsonString = """
        {
            "status1": "ACTIVE",
            "status2": "PENDING"
        }
    """
        val input: JsonNode = objectMapper.readTree(jsonString)
        val result = toKotlinObject(input, typeOf<Map<String, Status>>()) as Map<String, Status>

        assertThat(result).contains("status1" to Status.ACTIVE)
        assertThat(result).contains("status2" to Status.PENDING)
    }

}

data class SimpleExample(val name: String, val age: Int)
data class NestedExample(val title: String, val details: SimpleExample)
data class ListExample(val items: List<Int>)
data class MapExample(val properties: Map<String, Double>)
data class MixedExample(val values: List<Map<String, Int>>)
data class FloatExample(val value: Float)
data class DoubleExample(val value: Double)
data class NumberExample(val value: Number)
data class BooleanExample(val value: Boolean)
data class MixedTypesExample(
    val floatValue: Float,
    val doubleValue: Double,
    val numberValue: Number,
    val booleanValue: Boolean
)

enum class Status {
    ACTIVE,
    INACTIVE,
    PENDING
}
data class User(
    val username: String,
    val status: Status
)
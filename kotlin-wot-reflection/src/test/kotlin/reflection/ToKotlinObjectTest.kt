package reflection

import ai.ancf.lmos.wot.reflection.ThingBuilder.toKotlinObject
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ToKotlinObjectTests {

    @Test
    fun testPrimitiveString() {
        val input = DataSchemaValue.StringValue("Hello")
        val result = toKotlinObject(input, String::class.createType())
        assertEquals("Hello", result)
    }

    @Test
    fun testPrimitiveInt() {
        val input = DataSchemaValue.IntegerValue(42)
        val result = toKotlinObject(input, Int::class.createType())
        assertEquals(42, result)
    }

    @Test
    fun testPrimitiveBoolean() {
        val input = DataSchemaValue.BooleanValue(true)
        val result = toKotlinObject(input, Boolean::class.createType())
        assertEquals(true, result)
    }

    @Test
    fun testListOfPrimitives() {
        val input = DataSchemaValue.ArrayValue(
            listOf(
                1,
                2,
                3
            )
        )
        val result = toKotlinObject(input, List::class.createType(
            listOf(KTypeProjection.invariant(Int::class.createType()))
        ))
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testNestedDataClass() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "title" to "Nested Example",
                "details" to mapOf(
                    "name" to "John",
                    "age" to 30
                )
            )
        )
        val result = toKotlinObject(input, NestedExample::class.createType())
        val expected = NestedExample(title = "Nested Example", details = SimpleExample(name = "John", age = 30))
        assertEquals(expected, result)
    }

    @Test
    fun testListOfNestedDataClasses() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "items" to  listOf(
                    10,
                    20,
                    30
                )
            )
        )
        val result = toKotlinObject(input, ListExample::class.createType())
        val expected = ListExample(items = listOf(10, 20, 30))
        assertEquals(expected, result)
    }

    @Test
    fun testMapExample() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "properties" to mapOf(
                    "key1" to 1.1,
                    "key2" to 2.2
                )
            )
        )
        val result = toKotlinObject(input, MapExample::class.createType())
        val expected = MapExample(properties = mapOf("key1" to 1.1, "key2" to 2.2))
        assertEquals(expected, result)
    }

    @Test
    fun testListOfMaps() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "values" to listOf(
                    mapOf("key1" to 1),
                    mapOf("key2" to 2)
                )
            )
        )
        val result = toKotlinObject(input, MixedExample::class.createType())
        val expected = MixedExample(values = listOf(mapOf("key1" to 1), mapOf("key2" to 2)))
        assertEquals(expected, result)
    }

    // Test for Float property in a data class
    @Test
    fun testFloatInDataClass() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "value" to 3.14f
            )
        )
        val result = toKotlinObject(input, FloatExample::class.createType())
        val expected = FloatExample(value = 3.14f)
        assertEquals(expected, result)
    }

    // Test for Double property in a data class
    @Test
    fun testDoubleInDataClass() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "value" to 3.14159
            )
        )
        val result = toKotlinObject(input, DoubleExample::class.createType())
        val expected = DoubleExample(value = 3.14159)
        assertEquals(expected, result)
    }

    // Test for Number property in a data class (handles Int, Float, Double, etc.)
    @Test
    fun testNumberInDataClass() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "value" to 42
            )
        )
        val result = toKotlinObject(input, NumberExample::class.createType())
        val expected = NumberExample(value = 42)
        assertEquals(expected, result)
    }

    // Test for Boolean property in a data class
    @Test
    fun testBooleanInDataClass() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "value" to true
            )
        )
        val result = toKotlinObject(input, BooleanExample::class.createType())
        val expected = BooleanExample(value = true)
        assertEquals(expected, result)
    }

    // Test for List of Float
    @Test
    fun testListOfFloat() {
        val input = DataSchemaValue.ArrayValue(
            listOf(3.14f, 2.71f, 1.61f)
        )
        val result = toKotlinObject(input, List::class.createType(
            listOf(KTypeProjection.invariant(Float::class.createType()))
        ))
        assertEquals(listOf(3.14f, 2.71f, 1.61f), result)
    }

    // Test for List of Double
    @Test
    fun testListOfDouble() {
        val input = DataSchemaValue.ArrayValue(
            listOf(3.14159, 2.71828, 1.61803)
        )
        val result = toKotlinObject(input, List::class.createType(
            listOf(KTypeProjection.invariant(Double::class.createType()))
        ))
        assertEquals(listOf(3.14159, 2.71828, 1.61803), result)
    }

    // Test for MixedTypesExample with Float, Double, Number, and Boolean
    @Test
    fun testMixedTypesExample() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "floatValue" to 3.14f,
                "doubleValue" to 3.14159,
                "numberValue" to 42,        // Could be any numeric type like Int or Long
                "booleanValue" to true
            )
        )
        val result = toKotlinObject(input, MixedTypesExample::class.createType())
        val expected = MixedTypesExample(
            floatValue = 3.14f,
            doubleValue = 3.14159,
            numberValue = 42,           // The Number property can hold Int, Float, Double, etc.
            booleanValue = true
        )
        assertEquals(expected, result)
    }

    @Test
    fun testEnumConversion() {
        val input = DataSchemaValue.StringValue("ACTIVE")
        val result = toKotlinObject(input, Status::class.createType())
        assertEquals(Status.ACTIVE, result)
    }

    // Test for Enum in Data Class
    @Test
    fun testEnumInDataClass() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "username" to "john_doe",
                "status" to "PENDING"
            )
        )
        val result = toKotlinObject(input, User::class.createType())
        val expected = User(username = "john_doe", status = Status.PENDING)
        assertEquals(expected, result)
    }

    // Test for Enum with Invalid Value (Should throw an exception)
    @Test
    fun testEnumConversionWithInvalidValue() {
        val input = DataSchemaValue.StringValue("UNKNOWN")
        assertFailsWith<IllegalArgumentException> {
            toKotlinObject(input, Status::class.createType())
        }
    }

    // Test for Enum in List
    @Test
    fun testListOfEnums() {
        val input = DataSchemaValue.ArrayValue(
            listOf("ACTIVE", "INACTIVE", "PENDING")
        )
        val result = toKotlinObject(input, List::class.createType(
            listOf(KTypeProjection.invariant(Status::class.createType()))
        ))
        assertEquals(listOf(Status.ACTIVE, Status.INACTIVE, Status.PENDING), result)
    }

    // Test for Enum in Map
    @Test
    fun testMapOfEnums() {
        val input = DataSchemaValue.ObjectValue(
            mapOf(
                "status1" to "ACTIVE",
                "status2" to "PENDING"
            )
        )
        val result = toKotlinObject(input, Map::class.createType(
            listOf(KTypeProjection.invariant(String::class.createType()), KTypeProjection.invariant(Status::class.createType()))
        ))
        val expected = mapOf("status1" to Status.ACTIVE, "status2" to Status.PENDING)
        assertEquals(expected, result)
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
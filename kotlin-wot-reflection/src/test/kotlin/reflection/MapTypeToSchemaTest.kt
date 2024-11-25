package reflection

import ai.ancf.lmos.wot.reflection.ThingBuilder.mapTypeToSchema
import ai.ancf.lmos.wot.thing.schema.*
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTypeToSchemaTest {

    @Test
    fun testMapTypeToSchema_Primitives() {
        assertEquals(IntegerSchema(), mapTypeToSchema(typeOf<Int>()))
        assertEquals(NumberSchema(), mapTypeToSchema(typeOf<Float>()))
        assertEquals(NumberSchema(), mapTypeToSchema(typeOf<Double>()))
        assertEquals(NumberSchema(), mapTypeToSchema(typeOf<Long>()))
        assertEquals(StringSchema(), mapTypeToSchema(typeOf<String>()))
        assertEquals(BooleanSchema(), mapTypeToSchema(typeOf<Boolean>()))
        assertEquals(NullSchema(), mapTypeToSchema(typeOf<Unit>()))
    }

    @Test
    fun testMapTypeToSchema_Collection() {
        // List<String> example
        val listType = typeOf<List<String>>()
        assertEquals(ArraySchema(items = StringSchema()), mapTypeToSchema(listType))

        // Set<Int> example
        val setType = typeOf<Set<Int>>()
        assertEquals(ArraySchema(items = IntegerSchema()), mapTypeToSchema(setType))
    }

    @Test
    fun testMapTypeToSchema_Array() {
        // Array<String> example
        assertEquals(ArraySchema(items = StringSchema()), mapTypeToSchema(typeOf<Array<String>>()))

        // Array<Number> example
        assertEquals(ArraySchema(items = NumberSchema()), mapTypeToSchema(typeOf<Array<Number>>()))

        // Array<Int> example
        assertEquals(ArraySchema(items = IntegerSchema()), mapTypeToSchema(typeOf<Array<Int>>()))
    }

    @Test
    fun testMapTypeToSchema_CustomObject() {
        // Custom class mapping, e.g., MyClass
        // Assuming that buildObjectSchema() can handle custom classes
        val objectSchema = ObjectSchema().apply {
            properties["id"] = IntegerSchema()
            properties["name"] = StringSchema()
            required += listOf("id", "name")
        }

        assertEquals(objectSchema, mapTypeToSchema(typeOf<MyClass>()))
    }

    @Test
    fun testMapTypeToSchema_CustomObjectWithOptionalProperties() {
        // Custom class mapping, e.g., MyClass
        // Assuming that buildObjectSchema() can handle custom classes
        val objectSchema = ObjectSchema().apply {
            properties["id"] = IntegerSchema()
            properties["name"] = StringSchema()
            required += listOf("id")
        }

        assertEquals(objectSchema, mapTypeToSchema(typeOf<MyClassWithOptional>()))
    }

    // Assuming the following simple class for testing:
    data class MyClass(val id: Int, val name: String)
    data class MyClassWithOptional(val id: Int, val name: String?)
}
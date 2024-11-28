package ai.ancf.lmos.wot.reflection

import ai.ancf.lmos.wot.reflection.ExposedThingBuilder.buildObjectSchema
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.IntegerSchema
import ai.ancf.lmos.wot.thing.schema.StringSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildObjectSchemaTest {

    // Function with non-nullable parameters (MyClass)
    fun functionWithRequiredParameters(id: Int, name: String) {}

    // Function with nullable parameters (MyClassWithOptional)
    fun functionWithNullableParameter(id: Int, name: String?) {}

    // Function with a mix of nullable and non-nullable parameters (MyClassMixed)
    fun functionWithMixedParameters(id: Int, name: String?, age: Int = 30) {}

    // Function with a parameter having a default value (optional)
    fun functionWithOptionalParameter(id: Int, name: String = "Guest", age: Int = 30) {}

    // Test case 1: Non-nullable parameters (myClass)
    @Test
    fun `test buildObjectSchema with non-nullable parameters`() {
        val parameterTypes = ::functionWithRequiredParameters.parameters

        val schema = buildObjectSchema(parameterTypes)

        val expectedProperties: Map<String, DataSchema<*>> = mapOf(
            "id" to IntegerSchema(),
            "name" to StringSchema()
        )
        val expectedRequired = listOf("id", "name")

        assertEquals(expectedProperties, schema.properties)
        assertEquals(expectedRequired, schema.required)
    }

    // Test case 2: Nullable parameters (myClassWithOptional)
    @Test
    fun `test buildObjectSchema with nullable parameters`() {
        val parameterTypes = ::functionWithNullableParameter.parameters

        val schema = buildObjectSchema(parameterTypes)

        val expectedProperties: Map<String, DataSchema<*>> = mapOf(
            "id" to IntegerSchema(),
            "name" to StringSchema() // nullable but included as a property
        )
        val expectedRequired = listOf("id")  // name is nullable, so not required

        assertEquals(expectedProperties, schema.properties)
        assertEquals(expectedRequired, schema.required)
    }

    // Test case 3: Mix of nullable and non-nullable parameters (myClassMixed)
    @Test
    fun `test buildObjectSchema with a mix of nullable and non-nullable parameters`() {
        val parameterTypes = ::functionWithMixedParameters.parameters

        val schema = buildObjectSchema(parameterTypes)

        val expectedProperties: Map<String, DataSchema<*>> = mapOf(
            "id" to IntegerSchema(),
            "name" to StringSchema(),
            "age" to IntegerSchema()
        )
        val expectedRequired = listOf("id")  // name is nullable, so not required

        assertEquals(expectedProperties, schema.properties)
        assertEquals(expectedRequired, schema.required)
    }

    // Test case 4: Function with optional parameters (greet)
    @Test
    fun `test optional parameter`() {
        // Get the parameters of the 'greet' function using reflection
        val parameterTypes = ::functionWithOptionalParameter.parameters

        val schema = buildObjectSchema(parameterTypes)

        val expectedProperties: Map<String, DataSchema<*>> = mapOf(
            "id" to IntegerSchema(),
            "name" to StringSchema(),
            "age" to IntegerSchema()
        )
        val expectedRequired = listOf("id")  // name is nullable, so not required

        assertEquals(expectedProperties, schema.properties)
        assertEquals(expectedRequired, schema.required)
    }
}
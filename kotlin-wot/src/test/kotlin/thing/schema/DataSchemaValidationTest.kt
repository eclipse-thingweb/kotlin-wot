/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.schema

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class DataSchemaValidationTest {

    // StringSchema Tests
    @Test
    fun `test valid string with valid length`() {
        val schema = StringSchema(minLength = 5, maxLength = 10)
        assertTrue(schema.validate("valid").isEmpty())
        assertTrue(schema.validate("another").isEmpty())
    }

    @Test
    fun `test invalid string with too short length`() {
        val schema = StringSchema(minLength = 5, maxLength = 10)
        val exceptions = schema.validate("shor")
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is StringLengthException })
    }

    @Test
    fun `test invalid string with too long length`() {
        val schema = StringSchema(minLength = 5, maxLength = 10)
        val exceptions = schema.validate("thisisaverylongstring")
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is StringLengthException })
    }

    @Test
    fun `test valid string with valid pattern`() {
        val schema = StringSchema(minLength = 5, maxLength = 10, pattern = "^[A-Za-z]+$")
        assertTrue(schema.validate("Valid").isEmpty())
    }

    @Test
    fun `test invalid string with invalid pattern`() {
        val schema = StringSchema(minLength = 5, maxLength = 20, pattern = "^[A-Za-z]+$")
        val exceptions = schema.validate("Invalid@123")
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is PatternException })
    }

    // IntegerSchema Tests
    @Test
    fun `test valid integer in range`() {
        val schema = IntegerSchema(minimum = 5, maximum = 10)
        assertTrue(schema.validate(7).isEmpty())
    }

    @Test
    fun `test invalid integer below min`() {
        val schema = IntegerSchema(minimum = 5, maximum = 10)
        val exceptions = schema.validate(3)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is BoundsException })
    }

    @Test
    fun `test invalid integer above max`() {
        val schema = IntegerSchema(minimum = 5, maximum = 10)
        val exceptions = schema.validate(15)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is BoundsException })
    }

    // ArraySchema Tests
    @Test
    fun `test valid array with valid length and unique items`() {
        val stringSchema = StringSchema(minLength = 3, maxLength = 5)
        val arraySchema = ArraySchema(minItems = 2, maxItems = 5, items = stringSchema)

        val validArray = listOf("abc", "def")
        assertTrue(arraySchema.validate(validArray).isEmpty())
    }

    @Test
    fun `test invalid array with invalid length`() {
        val stringSchema = StringSchema(minLength = 3, maxLength = 5)
        val arraySchema = ArraySchema(minItems = 2, maxItems = 5, items = stringSchema)

        val invalidArray = listOf("abc")
        val exceptions = arraySchema.validate(invalidArray)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is ArrayLengthException })
    }

    @Test
    fun `test valid array with valid item type`() {
        val intSchema = IntegerSchema(minimum = 5, maximum = 10)
        val arraySchema = ArraySchema(minItems = 2, maxItems = 5, items = intSchema)

        val validArray = listOf(6, 8, 5)
        assertTrue(arraySchema.validate(validArray).isEmpty())
    }

    @Test
    fun `test invalid array with invalid item type`() {
        val intSchema = IntegerSchema(minimum = 5, maximum = 10)
        val arraySchema = ArraySchema(minItems = 2, maxItems = 5, items = intSchema)

        val invalidArray = listOf(6, 8, "string")
        val exceptions = arraySchema.validate(invalidArray)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is ArrayItemsException })
    }

    // BooleanSchema Tests
    @Test
    fun `test valid boolean value`() {
        val schema = BooleanSchema()
        assertTrue(schema.validate(true).isEmpty())
        assertTrue(schema.validate(false).isEmpty())
    }

    // NumberSchema Tests
    @Test
    fun `test valid number in range`() {
        val schema = NumberSchema(minimum = 5.0, maximum = 10.0)
        assertTrue(schema.validate(7.0).isEmpty())
        assertTrue(schema.validate(5.0).isEmpty())  // Valid lower bound
        assertTrue(schema.validate(10.0).isEmpty()) // Valid upper bound
    }

    @Test
    fun `test invalid number below min`() {
        val schema = NumberSchema(minimum = 5.0, maximum = 10.0)
        val exceptions = schema.validate(4.0)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is BoundsException })
    }

    @Test
    fun `test invalid number above max`() {
        val schema = NumberSchema(minimum = 5.0, maximum = 10.0)
        val exceptions = schema.validate(11.0)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is BoundsException })
    }

    // Validators Tests
    @Test
    fun `test validateArrayLength`() {
        val validArray = listOf(1, 2, 3)
        val invalidArray = listOf(1)

        assertTrue(Validators.validateArrayLength(validArray, 1, 5).isEmpty())
        val exceptions = Validators.validateArrayLength(invalidArray, 2, 5)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is ArrayLengthException })
    }

    @Test
    fun `test valid object with valid properties`() {
        val objectSchema = objectSchema {
            stringProperty("name") {
                minLength = 3
                maxLength = 10
            }
            integerProperty("age") {
                minimum = 18
                maximum = 100
            }
        }

        // Valid object
        val validObject = mapOf(
            "name" to "John",
            "age" to 25
        )

        assertTrue { objectSchema.validate(validObject).isEmpty() }
    }

    @Test
    fun `test invalid object with invalid properties`() {
        val objectSchema = objectSchema {
            stringProperty("name") {
                minLength = 3
                maxLength = 10
            }
            integerProperty("age") {
                minimum = 18
                maximum = 100
            }
        }

        // Invalid object
        val invalidObject = mapOf(
            "name" to "Jo", // Too short
            "age" to 17     // Below minimum
        )

        val exceptions = objectSchema.validate(invalidObject)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.any { it is StringLengthException })
        assertTrue(exceptions.any { it is BoundsException })
    }

    @Test
    fun `test missing required property`() {
        val objectSchema = objectSchema {
            stringProperty("name") {
                minLength = 3
                maxLength = 10
            }
            integerProperty("age") {
                minimum = 18
                maximum = 100
            }
            required.add("name")  // Adding 'name' as a required field
        }

        // Missing the required 'name' property
        val invalidObject = mapOf("age" to 25)

        val exceptions = objectSchema.validate(invalidObject)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.any { it is ArrayItemsException })
    }

    @Test
    fun `test object with nested object schema`() {
        val objectSchema = objectSchema {
            stringProperty("name") {
                minLength = 3
                maxLength = 10
            }
            objectProperty("address") {
                stringProperty("street") {
                    minLength = 5
                }
                integerProperty("zipcode") {
                    minimum = 10000
                    maximum = 99999
                }
            }
        }

        val validObject = mapOf(
            "name" to "John",
            "address" to mapOf(
                "street" to "Elm St",
                "zipcode" to 12345
            )
        )

        assertTrue { objectSchema.validate(validObject).isEmpty() }
    }

    @Test
    fun `test object with invalid nested object schema`() {
        val objectSchema = objectSchema {
            stringProperty("name") {
                minLength = 3
                maxLength = 10
            }
            objectProperty("address") {
                stringProperty("street") {
                    minLength = 5
                }
                integerProperty("zipcode") {
                    minimum = 10000
                    maximum = 99999
                }
            }
        }

        val invalidObject = mapOf(
            "name" to "John",
            "address" to mapOf(
                "street" to "Elm",  // Too short for street
                "zipcode" to 1234   // Invalid zipcode
            )
        )

        val exceptions = objectSchema.validate(invalidObject)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.any { it is StringLengthException })
        assertTrue(exceptions.any { it is BoundsException })
    }

    @Test
    fun `test object with enum validation`() {
        val objectSchema = objectSchema {
            stringProperty("status") {
                enum = listOf("active", "inactive")
            }
        }

        val validObject = mapOf("status" to "active")
        val invalidObject = mapOf("status" to "pending")

        assertTrue { objectSchema.validate(validObject).isEmpty() }
        val exceptions = objectSchema.validate(invalidObject)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is EnumException })
    }

    @Test
    fun `test array schema with nested arrays`() {
        val innerArraySchema = ArraySchema(
            minItems = 1,
            maxItems = 3,
            items = IntegerSchema(minimum = 1, maximum = 10)
        )
        val outerArraySchema = ArraySchema(
            minItems = 1,
            maxItems = 2,
            items = innerArraySchema
        )

        val validNestedArray = listOf(
            listOf(1, 2, 3),
            listOf(4, 5)
        )
        val invalidNestedArray = listOf(
            listOf(1, 2, 3, 4), // Exceeds maxItems of inner array
            listOf(5)
        )

        assertTrue(outerArraySchema.validate(validNestedArray).isEmpty())
        val exceptions = outerArraySchema.validate(invalidNestedArray)
        assertFalse(exceptions.isEmpty())
        assertTrue(exceptions.all { it is ArrayLengthException })
    }
}
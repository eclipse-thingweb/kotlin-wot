/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing.schema

sealed class ValidationException(message: String) : Exception(message)

class StringLengthException(message: String) : ValidationException(message)
class PatternException(message: String) : ValidationException(message)
class EnumException(message: String) : ValidationException(message)
class BoundsException(message: String) : ValidationException(message)
class MultipleOfException(message: String) : ValidationException(message)
class ConstException(message: String) : ValidationException(message)
class ArrayLengthException(message: String) : ValidationException(message)
class ArrayItemsException(message: String) : ValidationException(message)


object Validators {

    fun validateStringLength(value: String, minLength: Int?, maxLength: Int?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (minLength != null && value.length < minLength) {
            exceptions.add(StringLengthException("String length is less than $minLength"))
        }
        if (maxLength != null && value.length > maxLength) {
            exceptions.add(StringLengthException("String length is greater than $maxLength"))
        }
        return exceptions
    }

    fun validatePattern(value: String, pattern: String?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (pattern != null && !Regex(pattern).matches(value)) {
            exceptions.add(PatternException("String does not match pattern $pattern"))
        }
        return exceptions
    }

    fun validateEnum(value: Any, enum: List<Any>?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (enum != null && !enum.contains(value)) {
            exceptions.add(EnumException("Value $value is not in the enum $enum"))
        }
        return exceptions
    }

    fun validateBounds(value: Number, minimum: Number?, maximum: Number?, exclusiveMinimum: Number?, exclusiveMaximum: Number?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        val doubleValue = value.toDouble()
        if (minimum != null && doubleValue < minimum.toDouble()) {
            exceptions.add(BoundsException("Value $value is less than minimum $minimum"))
        }
        if (maximum != null && doubleValue > maximum.toDouble()) {
            exceptions.add(BoundsException("Value $value is greater than maximum $maximum"))
        }
        if (exclusiveMinimum != null && doubleValue <= exclusiveMinimum.toDouble()) {
            exceptions.add(BoundsException("Value $value is less than or equal to exclusive minimum $exclusiveMinimum"))
        }
        if (exclusiveMaximum != null && doubleValue >= exclusiveMaximum.toDouble()) {
            exceptions.add(BoundsException("Value $value is greater than or equal to exclusive maximum $exclusiveMaximum"))
        }
        return exceptions
    }

    fun validateMultipleOf(value: Int, multipleOf: Int?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (multipleOf != null && value % multipleOf != 0) {
            exceptions.add(MultipleOfException("Value $value is not a multiple of $multipleOf"))
        }
        return exceptions
    }

    fun validateMultipleOf(value: Number, multipleOf: Number?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (multipleOf != null && value.toDouble() % multipleOf.toDouble() != 0.0) {
            exceptions.add(MultipleOfException("Value $value is not a multiple of $multipleOf"))
        }
        return exceptions
    }

    fun validateConst(value: Boolean, const: Boolean?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (const != null && value != const) {
            exceptions.add(ConstException("Value $value does not match constant $const"))
        }
        return exceptions
    }

    fun validateArrayLength(value: List<*>, minItems: Int?, maxItems: Int?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (minItems != null && value.size < minItems) {
            exceptions.add(ArrayLengthException("Array size is less than $minItems"))
        }
        if (maxItems != null && value.size > maxItems) {
            exceptions.add(ArrayLengthException("Array size is greater than $maxItems"))
        }
        return exceptions
    }

    fun validateArrayItems(value: List<*>, itemsSchema: DataSchema<*>?): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        if (itemsSchema != null) {
            value.forEach { item ->
                val itemExceptions = when (itemsSchema) {
                    is StringSchema -> if (item is String) itemsSchema.validate(item) else listOf(ArrayItemsException("Item $item is not a String"))
                    is IntegerSchema -> if (item is Int) itemsSchema.validate(item) else listOf(ArrayItemsException("Item $item is not an Integer"))
                    is BooleanSchema -> if (item is Boolean) itemsSchema.validate(item) else listOf(ArrayItemsException("Item $item is not a Boolean"))
                    is NumberSchema -> if (item is Number) itemsSchema.validate(item) else listOf(ArrayItemsException("Item $item is not a Number"))
                    is ObjectSchema -> if (item is Map<*, *>) itemsSchema.validate(item) else listOf(ArrayItemsException("Item $item is not an Object"))
                    is ArraySchema<*> -> if (item is List<*>) itemsSchema.validate(item) else listOf(ArrayItemsException("Item $item is not an Array"))
                    else -> listOf(ArrayItemsException("Unknown item type"))
                }
                exceptions.addAll(itemExceptions)
            }
        }
        return exceptions
    }
}
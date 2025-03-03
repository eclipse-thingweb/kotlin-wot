/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.reflection

import ai.ancf.lmos.wot.reflection.ExposedThingBuilder.addActionHandler
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder.addEventHandler
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder.addPropertyHandler
import ai.ancf.lmos.wot.thing.ExposedThing
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.reflect.*
import kotlin.test.Test

class AddHandlerTest {

    data class MyClass(val name: String)

    @Test
    fun `test addEventHandler should handle events correctly`() = runTest {
        // Mocking dependencies
        val exposedThing = mockk<ExposedThing>(relaxed = true)
        val eventFunction = mockk<KFunction<*>>()
        val instance = mockk<MyClass>(relaxed = true)

        // Calling the method
        val eventMap = mutableMapOf("eventName" to eventFunction)
        addEventHandler(eventMap, exposedThing, instance)

        // Verifying that event subscribe handler was set
        verify { exposedThing.setEventSubscribeHandler("eventName", any()) }
    }

    @Test
    fun `test addActionHandler should handle actions correctly`() {
        // Mocking dependencies
        val exposedThing = mockk<ExposedThing>(relaxed = true)
        val actionFunction = mockk<KFunction<*>>()
        val instance = mockk<MyClass>(relaxed = true)

        // Calling the method
        val actionMap = mutableMapOf("actionName" to actionFunction)
        addActionHandler(actionMap, exposedThing, instance)

        // Verifying that action handler was set
        verify { exposedThing.setActionHandler("actionName", any()) }
    }

    @Test
    fun `test addPropertyHandler should handle read-only properties`() {
        // Mocking dependencies
        val exposedThing = mockk<ExposedThing>(relaxed = true)
        val readOnlyProperty = mockk<KProperty1<MyClass, *>>()
        val instance = MyClass("test")
        val returnType = mockk<KType>()
        val classifier = mockk<KClassifier>()

        // Mocking getter for property
        every { readOnlyProperty.returnType } returns returnType
        every { returnType.classifier } returns classifier
        every { readOnlyProperty.getter.call(instance) } returns "testValue"

        // Maps for read-only, write-only, and read-write properties
        val readOnlyPropertiesMap = mutableMapOf("readOnlyProperty" to readOnlyProperty)
        val writeOnlyPropertiesMap = mutableMapOf<String, KMutableProperty1<MyClass, *>>()
        val readWritePropertiesMap = mutableMapOf<String, KMutableProperty1<MyClass, *>>()

        // Calling the method for read-only properties
        addPropertyHandler(readOnlyPropertiesMap, exposedThing, instance, writeOnlyPropertiesMap, readWritePropertiesMap)

        // Verifying that property read handler was set
        verify { exposedThing.setPropertyReadHandler("readOnlyProperty", any()) }

    }

    @Test
    fun `test addPropertyHandler should handle write-only properties`() {
        // Mocking dependencies
        val exposedThing = mockk<ExposedThing>(relaxed = true)
        val writeOnlyProperty = mockk<KMutableProperty1<MyClass, *>>()
        val instance = MyClass("test")

        // Mocking setter for write-only property
        val setter : KMutableProperty1.Setter<MyClass, out Any?> = mockk()
        every { writeOnlyProperty.setter } returns setter

        // Maps for read-only, write-only, and read-write properties
        val readOnlyPropertiesMap = mutableMapOf<String, KProperty1<MyClass, *>>()
        val writeOnlyPropertiesMap = mutableMapOf("writeOnlyProperty" to writeOnlyProperty)
        val readWritePropertiesMap = mutableMapOf<String, KMutableProperty1<MyClass, *>>()

        // Calling the method for write-only properties
        addPropertyHandler(readOnlyPropertiesMap, exposedThing, instance, writeOnlyPropertiesMap, readWritePropertiesMap)

        // Verifying that property write handler was set
        verify { exposedThing.setPropertyWriteHandler("writeOnlyProperty", any()) }

    }
}
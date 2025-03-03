/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package reflection

import org.eclipse.thingweb.Servient
import org.eclipse.thingweb.Wot
import org.eclipse.thingweb.reflection.ExposedThingBuilder
import org.eclipse.thingweb.reflection.things.ComplexThing
import org.eclipse.thingweb.thing.schema.WoTExposedThing
import org.eclipse.thingweb.thing.schema.WoTThingDescription
import kotlin.test.BeforeTest

class ConsumedThingBuilderTest {

    lateinit var servient: Servient
    lateinit var wot: Wot
    lateinit var complexThing: ComplexThing
    lateinit var exposedThing: WoTExposedThing
    lateinit var thingDescription: WoTThingDescription

    @BeforeTest
    fun setUp() {
        // Set up the servient and WoT instance
        servient = Servient()
        wot = Wot.create(servient)

        // Create an instance of ComplexThing
        complexThing = ComplexThing()

        // Generate ThingDescription from the class
        exposedThing = ExposedThingBuilder.createExposedThing(wot, complexThing, ComplexThing::class) as WoTExposedThing
    }
}
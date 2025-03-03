/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package reflection

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder
import ai.ancf.lmos.wot.reflection.things.ComplexThing
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import ai.ancf.lmos.wot.thing.schema.WoTThingDescription
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
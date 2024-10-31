package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ThingProperty
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThingTest {
    private lateinit var objectType: Type
    private lateinit var objectContext: Context
    private lateinit var id: String
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var base: String
    private lateinit var titles: MutableMap<String, String>
    private lateinit var descriptions: MutableMap<String, String>
    private lateinit var properties: Map<String, ThingProperty<Any>>
    private lateinit var actions: Map<String, ThingAction<Any, Any>>
    private lateinit var events: Map<String, ThingEvent<Any>>
    private lateinit var securityDefinitions: MutableMap<String, SecurityScheme>
    private lateinit var forms: List<Form>
    private lateinit var security: List<String>

    @BeforeTest
    fun setUp() {
        objectType = Type("Thing")
        objectContext = Context("http://www.w3.org/ns/td")
        id = "foo"
        title = "Foo"
        description = "Bar"
        base = ""
        titles = mutableMapOf("de" to "Zähler")
        descriptions = mutableMapOf("de" to "Dies ist ein Zähler")
        properties = emptyMap()
        actions = emptyMap()
        events = emptyMap()
        securityDefinitions = mutableMapOf("basic_sc" to BasicSecurityScheme("header"))
        security = listOf("basic_sc")
        forms = listOf()
    }

    @Test
    fun toJson() {
        val thing = Thing(
            objectType = objectType,
            objectContext = objectContext,
            id = id,
            title = title,
            titles = titles,
            description = description,
            descriptions= descriptions,
            security = security,
            securityDefinitions = securityDefinitions
        )

        val thingAsJson = JsonMapper.instance.writeValueAsString(thing)
        assertThatJson(thingAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{    
                    "id":"foo",
                    "title":"Foo",
                    "titles":{"de":"Zähler"},
                    "description":"Bar",
                    "descriptions":{"de":"Dies ist ein Zähler"},
                    "@type":"Thing",
                    "@context":"http://www.w3.org/ns/td",
                    "securityDefinitions":{"basic_sc":{"scheme":"basic","in":"header"}},
                    "security":["basic_sc"]
                }"""
            )
    }

    @Test
    fun fromMap() {
        val map: Map<*, *> = java.util.Map.of(
            "id", "Foo",
            "description", "Bar",
            "@type", "Thing",
            "@context", listOf("http://www.w3.org/ns/td")
        )
        val thing: Thing = Thing.fromMap(map)
        assertEquals("Foo", thing.id)
        assertEquals("Bar", thing.description)
        assertEquals(Type("Thing"), thing.objectType)
        assertEquals(
            Context("http://www.w3.org/ns/td"),
            thing.objectContext
        )
    }

    @Test
    fun shouldDeserializeGivenJsonToThing() {
        val json = """{    
                    "id":"Foo",
                    "description":"Bar",
                    "@type":"Thing",
                    "@context":["http://www.w3.org/ns/td"],
                    "securityDefinitions": {
                        "basic_sc": {
                            "scheme": "basic",
                            "in": "header"
                        }
                    },    
                    "security": ["basic_sc"]
                }"""
        val thing = Thing.fromJson(json)
        if (thing != null) {
            assertEquals("Foo", thing.id)
            assertEquals("Bar", thing.description)
            assertEquals(Type("Thing"), thing.objectType)
            assertEquals(
                Context("http://www.w3.org/ns/td"),
                thing.objectContext
            )
            assertEquals(
                thing.securityDefinitions["basic_sc"], BasicSecurityScheme("header")
            )
            assertEquals(listOf("basic_sc"), thing.security)
        }
    }

    @Test
    fun testEquals() {
        val thingA = Thing(id = "id")
        val thingB = Thing(id = "id")
        assertEquals(thingA, thingB)
    }

    @Test
    fun getExpandedObjectType() {
        val thing = Thing(id = "foo",
            description = "Bar",
            objectType =  Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
                .addContext("saref", "https://w3id.org/saref#"))

        assertEquals("https://w3id.org/saref#Temperature",
            thing.getExpandedObjectType("saref:Temperature"))
    }
}
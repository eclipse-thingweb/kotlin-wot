package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ThingProperty
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import thing.schema.VariableSchema
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThingTest {
    private var objectType: Type? = null
    private var objectContext: Context? = null
    private var id: String = "foo"
    private var title: String? = null
    private var description: String? = null
    private var base: String? = null
    private var titles: Map<String, String>? = null
    private var descriptions: Map<String, String>? = null
    private var properties: Map<String, ThingProperty<VariableSchema>>? = null
    private var actions: Map<String, ThingAction<VariableSchema, VariableSchema>>? = null
    private var events: Map<String, ThingEvent<VariableSchema>>? = null
    private var securityDefinitions: Map<String, SecurityScheme>? = null
    private var forms: List<Form>? = null
    private var security: List<String>? = null
    private var metadata: Map<String, VariableSchema>? = null
    @BeforeTest
    fun setUp() {
        objectType = Type("Thing")
        objectContext = Context("http://www.w3.org/ns/td")
        title = "Foo"
        description = "Bar"
        base = ""
        titles = mapOf("de" to "Zähler")
        descriptions = mapOf("de" to "Dies ist ein Zähler")
        properties = emptyMap()
        actions = emptyMap()
        events = emptyMap()
        securityDefinitions = mapOf("basic_sc" to BasicSecurityScheme("header"))
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
        //val jsonString = Json.encodeToString(thing)
        //println(jsonString)

        val thingAsJson = thing.toJson()
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
                java.util.Map.of<String, BasicSecurityScheme>(
                    "basic_sc",
                    BasicSecurityScheme("header")
                ), thing.securityDefinitions
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
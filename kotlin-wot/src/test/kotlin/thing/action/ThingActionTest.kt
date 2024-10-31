package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.StringSchema
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class ThingActionTest {
    @Test
    fun testEquals() {
        val action = ThingAction(input = StringSchema(), output = StringSchema())
        assertEquals(StringSchema(), action.input)
        assertEquals(StringSchema(), action.output)
    }

    @Test
    fun testToJson() {
        val action = ThingAction(title = "title", description = "blabla", input = StringSchema(), output = StringSchema())
        val json = JsonMapper.instance.writeValueAsString(action)

        JsonAssertions.assertThatJson(json)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                    "title":"title",
                    "description":"blabla",
                    "input":{"type":"string"},
                    "output":{"type":"string"}
                    }
                """
            )
    }

}
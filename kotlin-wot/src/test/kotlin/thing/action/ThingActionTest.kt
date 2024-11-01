package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.StringSchema
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals


class ThingActionTest {
    @Test
    fun testEquals() {
        val action1 = ThingAction(input = StringSchema(), output = StringSchema())
        val action2 = ThingAction(input = StringSchema(), output = StringSchema())
        assertEquals(action1, action2)
    }

    @Test
    fun testHashCode() {
        val action1 = ThingAction(input = StringSchema(), output = StringSchema()).hashCode()
        val action2 = ThingAction(input = StringSchema(), output = StringSchema()).hashCode()
        assertEquals(action1, action2)
    }

    @Test
    fun testToJson() {
        val action = ThingAction(
            title = "title",
            description = "blabla",
            input = StringSchema(),
            output = StringSchema())
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
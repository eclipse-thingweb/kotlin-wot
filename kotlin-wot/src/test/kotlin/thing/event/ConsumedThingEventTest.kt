package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.ConsumedThingImpl
import ai.ancf.lmos.wot.thing.action.ConsumedThingEvent
import ai.ancf.lmos.wot.thing.action.ConsumedThingException
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.StringSchema
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConsumedThingEventTest {
    private lateinit var event: ThingEvent<String, String, String>
    private lateinit var thing: ConsumedThingImpl
    private lateinit var client: ProtocolClient
    private lateinit var form: Form
    private lateinit var consumedThingEvent: ConsumedThingEvent<String, String, String>

    @BeforeEach
    fun setUp() {
        event = mockk()
        thing = mockk()
        client = mockk()
        form = mockk()
        consumedThingEvent = ConsumedThingEvent(event, thing)
    }
    @Test
    fun `observe should return flow of events`() = runTest {
        // Arrange
        val expectedBody = """"value"""".toByteArray()
        val content1 = Content(type = "application/json", body = expectedBody)
        val content2 = Content(type = "application/json", body = expectedBody)

        val flow = flow {
            emit(content1)
            emit(content2)
        }

        // Mocking
        every { consumedThingEvent.forms } returns mutableListOf()
        every { consumedThingEvent.data } returns StringSchema()
        every { thing.getClientFor(any<List<Form>>(), Operation.SUBSCRIBE_EVENT) } returns Pair(client, form)
        coEvery { client.observeResource(form) } returns flow

        // Act
        val observedValues = mutableListOf<String>()
        consumedThingEvent.observe().collect { observedValues.add(it) }

        // Assert
        assertEquals(listOf("value", "value"), observedValues)
    }

    @Test
    fun `observe should throw ConsumedThingException when ProtocolClientException is thrown`() = runTest {
        // Arrange
        every { consumedThingEvent.forms } returns mutableListOf()
        every { consumedThingEvent.forms } returns mutableListOf()

        coEvery { consumedThingEvent.observe() } throws ProtocolClientException("Error")

        // Act & Assert
        val observedValues = mutableListOf<String>()
        val exception = assertFailsWith<ConsumedThingException> {
            consumedThingEvent.observe().collect { observedValues.add(it) }
        }
        assertEquals("Error", exception.cause?.message)
    }
}
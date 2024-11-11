package ai.ancf.lmos.wot.thing


import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.StringProperty
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtocolHelpersTest {

    @Test
    fun findRequestMatchingFormIndexReturnsCorrectIndex() {
        val forms = listOf(
            Form(href = "http://example.com/users", contentType = "application/json"),
            Form(href = "http://example.com/users/{userId}", contentType = "application/json")
        )
        val result = findRequestMatchingFormIndex(forms, "http", "users")
        assertEquals(0, result)
    }

    @Test
    fun findRequestMatchingFormIndexReturnsZeroForNullForms() {
        val result = findRequestMatchingFormIndex(null, "http", "users")
        assertEquals(0, result)
    }

    @Test
    fun findRequestMatchingFormIndexReturnsZeroForNoMatch() {
        val forms = listOf(
            Form(href = "http://example.com/users", contentType = "application/json")
        )
        val result = findRequestMatchingFormIndex(forms, "http", "posts")
        assertEquals(0, result)
    }

    @Test
    fun getFormIndexForOperationReturnsCorrectIndex() {
        val interaction = StringProperty().apply {
            forms = mutableListOf(
                Form(href = "http://example.com/users", op = listOf(Operation.READ_PROPERTY)),
                Form(href = "http://example.com/users", op = listOf(Operation.WRITE_PROPERTY))
            )
            readOnly = false
            writeOnly = false
        }
        val result = getFormIndexForOperation(interaction, "property", Operation.WRITE_PROPERTY)
        assertEquals(1, result)
    }

    @Test
    fun getFormIndexForOperationReturnsMinusOneForInvalidFormIndex() {
        val interaction =  StringProperty().apply {
            forms = mutableListOf(
                Form(href = "http://example.com/users", op = listOf(Operation.READ_PROPERTY))
            )
            readOnly = false
            writeOnly = false
        }
        val result = getFormIndexForOperation(interaction, "property", Operation.WRITE_PROPERTY, 1)
        assertEquals(-1, result)
    }

    @Test
    fun getPropertyOpValuesReturnsCorrectOperations() {
        val property =  StringProperty().apply {
            forms = mutableListOf()
            readOnly = false
            writeOnly = false
            observable = true
        }
        val result = getPropertyOpValues(property)
        assertEquals(listOf(Operation.WRITE_PROPERTY, Operation.READ_PROPERTY, Operation.OBSERVE_PROPERTY, Operation.UNOBSERVE_PROPERTY), result)
    }

    @Test
    fun getPropertyOpValuesReturnsEmptyListForReadOnlyAndWriteOnly() {
        val property =  StringProperty().apply {
            forms = mutableListOf()
            readOnly = true
            writeOnly = true
            observable = false
        }
        val result = getPropertyOpValues(property)
        assertEquals(emptyList(), result)
    }
}
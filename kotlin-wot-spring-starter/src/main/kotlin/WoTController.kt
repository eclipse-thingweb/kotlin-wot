package ai.anfc.lmos.wot.spring

import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/things")
class WoTController(private val things: Map<String, ExposedThing>) {

    @GetMapping("/{id}")
    fun getThing(@PathVariable id: String): ExposedThing {
        return things[id] ?: throw ThingNotFound("Thing not found with id: $id")
    }

    @PostMapping("/{id}/actions/{actionName}")
    fun invokeAction(@PathVariable id: String, @PathVariable actionName: String?): Any {

        val thing = things[id] ?: throw ThingNotFound("Thing not found with id: $id")
        val action = thing.actions[actionName] ?: throw ActionNotFound("Action not found with name: $actionName")

        return action.invoke().get()
    }

    @GetMapping("/{id}/properties")
    fun readAllProperties(
        @PathVariable id: String
    ): Any? {
        val thing = things[id] ?: throw ThingNotFound("Thing not found with id: $id")
        return ResponseEntity.ok(thing.properties)
    }

    @GetMapping("/{id}/properties/{propertyName}")
    fun readProperty(
        @PathVariable id: String,
        @PathVariable propertyName: String?
    ): ExposedThingProperty<Any> {
        val thing = things[id] ?: throw ThingNotFound("Thing not found with id: $id")

        return thing.properties[propertyName]
            ?: throw PropertyNotFound("Property not found with name: $propertyName")
    }

    @PutMapping("/{id}/properties/{propertyName}")
    fun writeProperty(
        @PathVariable id: String,
        @PathVariable propertyName: String?,
        @RequestBody value: Any?
    ) {
        val thing = things[id] ?: throw ThingNotFound("Thing not found with id: $id")
        val property = thing.properties?.get(propertyName) ?: throw PropertyNotFound("Property not found with name: $propertyName")
        property.write(value)
    }


}